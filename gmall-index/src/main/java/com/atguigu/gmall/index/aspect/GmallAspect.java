package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Aspect
@Component
@Slf4j
public class GmallAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter<String> bloomFilter;

//    这个方法是放到Around之后执行的
//    @Before("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
//    public void beforeApp(JoinPoint joinPoint) {
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        Method method = signature.getMethod();
//        Object[] args = joinPoint.getArgs();
//        boolean contains = bloomFilter.contains(String.valueOf(args[0]));
//        if(!contains) {
//            log.info("对不起您要查找的信息不存在！");
//            return;
//        }
//    }

    //向切点添加
    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
    public Object aroundAop(ProceedingJoinPoint joinPoint) throws Throwable {
        //得到方法的参数
        Object[] args = joinPoint.getArgs();
        //获得方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //获得方法
        Method method = signature.getMethod();
        //获得注解
        GmallCache annotation = method.getAnnotation(GmallCache.class);
        //获得返回值类型
        Class<?> returnType = method.getReturnType();
        //获得类的名称
        String className = joinPoint.getTarget().getClass().getName();
        //添加布隆过滤器，如果数据库中没有那么直接直接拒绝，连缓存都不用查询
        boolean contains = bloomFilter.contains(String.valueOf(args[0]));
        if(!contains) {
            return null;
        }
        //获得缓存的名称
        List<Object> argsList = Arrays.asList(args);
        String cacheName = annotation.cachePreFix() + argsList;
        String json = this.redisTemplate.opsForValue().get(cacheName);
        //如果缓存中有那么直接放回
        if(StringUtils.isNotBlank(json)) {
            return JSON.parseObject(json,returnType);
        }
        //如果缓存中没有那么添加分布式锁
        String lockPreFix = annotation.locPreFix();
        RLock lock = this.redissonClient.getLock(lockPreFix + argsList);
        lock.lock();
        try {
            String json2 = this.redisTemplate.opsForValue().get(cacheName);
            //如果缓存中有那么直接放回
            if(StringUtils.isNotBlank(json2)) {
                return JSON.parseObject(json2,returnType);
            }
            //执行业务逻辑
            Object proceed = joinPoint.proceed(args);
            //将查询到的结果保存到redis缓存中，随后将锁释放
            this.redisTemplate.opsForValue().set(cacheName,JSON.toJSONString(proceed),Long.valueOf(annotation.cacheTime() + new Random().nextInt(Integer.valueOf(annotation.random()))),annotation.TIME_UNIT());
            return proceed;
        } finally {
            lock.unlock();
        }
    }
}
