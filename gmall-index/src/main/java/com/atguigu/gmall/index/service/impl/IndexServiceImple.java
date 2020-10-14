package com.atguigu.gmall.index.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.index.utils.RedisLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.aopalliance.intercept.Joinpoint;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IndexServiceImple implements IndexService {

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private RedissonClient redissionClient;


    private AtomicInteger num = new AtomicInteger(0);
    private static final String PRE_FIX = "index:category:";
    private static final String PRE_FIX_LEVEL1 = "level1:";
    private static final String PRE_FIX_LEVEL2_LEVEL3 = "level2+3:";


    //此方法用于添加一级分类
    public List<CategoryEntity> index() {
        //如果redis中没有数据那么从数据库中查询并且添加到redis中
        System.out.println(redisTemplate.opsForValue().get(PRE_FIX + "level1:1"));
        System.out.println(StringUtils.isBlank(redisTemplate.opsForValue().get(PRE_FIX + "level1:1")));
        if (StringUtils.isBlank(redisTemplate.opsForValue().get(PRE_FIX + "level1:1"))) {
            ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoryByParentId(0L);
            List<CategoryEntity> data = listResponseVo.getData();
            for (CategoryEntity datum : data) {
                num.getAndIncrement();
                String flag = String.valueOf(num);
                String result = JSON.toJSONString(datum);
                redisTemplate.opsForValue().set(PRE_FIX + "level1:" + flag, result);
            }
            redisTemplate.opsForValue().set("index:category:level1:count", String.valueOf(num.intValue()));
            return data;
        } else {
            //直接从redis中添加数据
            List<CategoryEntity> data = new ArrayList<>();
            String level1Count = redisTemplate.opsForValue().get("index:category:level1:count");
            Integer levelCount2 = Integer.valueOf(level1Count);
            for (int i = 1; i <= levelCount2; i++) {
                System.out.println(num.intValue());
                String result = redisTemplate.opsForValue().get(PRE_FIX + "level1:" + i);
                System.out.println(result);
                CategoryEntity categoryEntity = JSON.parseObject(result, CategoryEntity.class);
                data.add(categoryEntity);
            }
            return data;
        }
    }



    //最终优化方案，使用注解
    @GmallCache(cachePreFix = "index:category:level2+3:")
    public List<CategoryEntity> getAllCates(Long cid) {
        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategories(cid);
        List<CategoryEntity> categoryEntityList = listResponseVo.getData();
        return categoryEntityList;
    }


    //使用redission分布式锁的优化方案
    public ResponseVo<List<CategoryEntity>> getAllCates0(Long cid) {
        String data = redisTemplate.opsForValue().get(PRE_FIX + PRE_FIX_LEVEL2_LEVEL3 + cid);
        if(StringUtils.isNotBlank(data)) {
            return ResponseVo.ok(JSON.parseArray(data,CategoryEntity.class));
        }
        //优化1：由于加锁是为了防止缓存击穿，因此并不需要全局的锁，只需要锁住对应的热点问题
        RLock lock = this.redissionClient.getLock("lock:" + cid);
        lock.lock();
        try {
            //优化2：添加锁以后继续查询缓存，如果存在的话直接返回
            String data2 = redisTemplate.opsForValue().get(PRE_FIX + PRE_FIX_LEVEL2_LEVEL3 + cid);
            if(StringUtils.isNotBlank(data2)) {
                return ResponseVo.ok(JSON.parseArray(data2,CategoryEntity.class));
            }
            ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategories(cid);
            List<CategoryEntity> categoryEntityList = listResponseVo.getData();
            if(CollectionUtils.isEmpty(categoryEntityList)) {
                this.redisTemplate.opsForValue().set(PRE_FIX + PRE_FIX_LEVEL2_LEVEL3 + cid,null,3,TimeUnit.MINUTES);
            } else {
                this.redisTemplate.opsForValue().set(PRE_FIX + PRE_FIX_LEVEL2_LEVEL3 + cid,JSON.toJSONString(categoryEntityList),30 + new Random().nextInt(5),TimeUnit.DAYS);
            }
            return listResponseVo;
        } finally {
            //优化3：为了防止死锁的发生，无论如何都要释放锁
            lock.unlock();
        }
    }


    //此方法用于动态生成二级分类以及一级分类
    public ResponseVo<List<CategoryEntity>> getAllCates1(Long cid) {
        String data = redisTemplate.opsForValue().get(PRE_FIX + PRE_FIX_LEVEL2_LEVEL3 + cid);
        //如果redis缓存中存在数据，那么从缓存中得到对应的数据直接返回
        if (StringUtils.isNotBlank(data)) {
            return ResponseVo.ok(JSON.parseArray(data, CategoryEntity.class));
        }
        //如果redis中没有目标数据，那么为了防止缓存穿透，即使数据库中不存在的数据我们也要添加到缓存中
        //做完了以上几步以后还需注意防止缓存穿透，为了防止某一热点数据在高访问量时过期，导致关系型数据库的并发压力，在这里应该使用分布式锁
        boolean lock = redisLock.lock("lock", String.valueOf(cid), 3L, TimeUnit.SECONDS);
        if (lock) {
            try {
                ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategories(cid);
                List<CategoryEntity> categoryEntityList = listResponseVo.getData();
                //数据库中不存在的数据缓存的时间要少一些，在这里将其设置为3分钟
                if (CollectionUtils.isEmpty(categoryEntityList)) {
                    redisTemplate.opsForValue().set(PRE_FIX + PRE_FIX_LEVEL2_LEVEL3 + cid, JSON.toJSONString(categoryEntityList), 3, TimeUnit.SECONDS);
                } else {
                    //如果是数据库中存在的数据，那么在redis中存放的时间要长一些，并且为了防止缓存雪崩要将其生命周期的值设置为不一致，这里我们用到了随机随机数random
                    redisTemplate.opsForValue().set(PRE_FIX + PRE_FIX_LEVEL2_LEVEL3 + cid, JSON.toJSONString(categoryEntityList), new Random().nextInt(5), TimeUnit.DAYS);
                }
                return listResponseVo;
            } finally {
                redisLock.unLock("lock", String.valueOf(cid));
            }
        }
        return null;
    }


    public ResponseVo<List<CategoryEntity>> getAllCates2(Long cid) {
        //第一步：根据cid从redis中查找是否存在数据
        String data = redisTemplate.opsForValue().get(PRE_FIX + "level2+3:" + cid);

        //如果redis中存在数据那么直接返回
        if (StringUtils.isNotBlank(data)) {
            List<CategoryEntity> categoryEntities = JSON.parseArray(data, CategoryEntity.class);
            return ResponseVo.ok(categoryEntities);
        }
        //1.如果redis中没有数据那么从数据库中查询数据，为了防止缓存击穿，要添加分布式锁
        //2.设置锁的过期时间，防止由于服务器宕机锁产生的死锁现象（获得锁了以后并没有释放）
        //3.将value的值设置成cid的目的是为了防止由于程序的运行时间大于锁的存活时间所造成的的“裸奔现象”
        //4.这段时间内有别的线程获得了锁，然而程序还在执行，因此当执行到结尾的时候会引起锁的误删，导致别的线程
        //的锁失效
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", String.valueOf(cid), 3, TimeUnit.SECONDS);
        if (lock) {
            ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategories(cid);
            List<CategoryEntity> categoryList = listResponseVo.getData();
            //为了防止缓存穿透，将不存在的数据进行添加，只是时间要缩短为存在五分钟
            if (CollectionUtils.isEmpty(categoryList)) {
                redisTemplate.opsForValue().set(PRE_FIX + "level2+3:" + cid, null, 3, TimeUnit.MINUTES);
            } else {
                //如果数据存在将数据存放到redis中并且返回
                String categoryListStr = JSON.toJSONString(categoryList);
                //为了防止缓存雪崩，为了避免某一时刻的redis缓存的集体失效，将其过期时间设置为不同的值
                redisTemplate.opsForValue().set(PRE_FIX + "level2+3:" + cid, categoryListStr, 30 + (long) (Math.random() * 10), TimeUnit.DAYS);
            }
            String endLock = redisTemplate.opsForValue().get("lock");
            //如果这把锁是这个线程的那么删除
            if (StringUtils.equals(String.valueOf(cid), endLock)) {
                redisTemplate.delete(String.valueOf(PRE_FIX + "level2+3:" + cid));
            }
            return ResponseVo.ok(JSON.parseArray(redisTemplate.opsForValue().get(PRE_FIX + "level2+3:" + cid), CategoryEntity.class));
        } else {
            this.getAllCates(cid);
        }
        return null;
    }


    public void test() {
        //测试redisssion的可重入锁
        RLock lock = redissionClient.getLock("lock");
        lock.lock();
        try {
            String count = redisTemplate.opsForValue().get("count");
            if (StringUtils.isBlank(count)) {
                redisTemplate.opsForValue().set("count", String.valueOf(1));
            }
            Integer num = Integer.valueOf(count);
            redisTemplate.opsForValue().set("count", String.valueOf(++num));
        } finally {
            lock.unlock();
        }
    }


    public void test2() {
        //由于本项目采用了微服务架构，因此为了防止因某一台服务器宕机而产生的死锁问题，因此给redis锁添加过期时间
        //为了防止由于业务复杂导致的锁的时间不足导致的误删问题，因此我们要给每一个线程添加对应的锁
        String postLock = UUID.randomUUID().toString().substring(0, 8);
        boolean lock = redisLock.lock("lock", postLock, 10L);
        try {
            if (lock) {
                String count = redisTemplate.opsForValue().get("count");
                if (StringUtils.isBlank(count)) {
                    redisTemplate.opsForValue().set("count", String.valueOf(1));
                }
                Integer num = Integer.valueOf(count);
                redisTemplate.opsForValue().set("count", String.valueOf(++num));
                //测试可重入
                //reentrantTest("lock",postLock,10L);
                try {
                    TimeUnit.SECONDS.sleep(60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            redisLock.unLock("lock", postLock);
        }

    }

    public void reentrantTest(String lockKey, String lockValue, Long lockTime) {
        this.redisLock.lock(lockKey, lockValue, lockTime);
        System.out.println("i love trump");
        this.redisLock.unLock(lockKey, lockValue);
    }

    public void testRead() {
        RReadWriteLock readWriteLock = this.redissionClient.getReadWriteLock("readWriteLock");
        readWriteLock.readLock().lock(10, TimeUnit.SECONDS);
        System.out.println("fuck you trump");
    }

    public void testWrite() {
        RReadWriteLock readWriteLock = this.redissionClient.getReadWriteLock("readWriteLock");
        readWriteLock.writeLock().lock(10, TimeUnit.SECONDS);
        System.out.println("fuck you america");
    }

    public String testLatch() {
        RCountDownLatch latch = redissionClient.getCountDownLatch("latch");
        latch.trySetCount(6L);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "fuck your daughter";
    }

    public String testCountDown() {
        RCountDownLatch latch = redissionClient.getCountDownLatch("latch");
        latch.countDown();
        return "fuck your wife";
    }
}
