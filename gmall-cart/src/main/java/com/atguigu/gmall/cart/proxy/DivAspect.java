package com.atguigu.gmall.cart.proxy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DivAspect {

    @Around("@annotation(DivAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        System.out.println("天王盖地虎");
        Object proceed = joinPoint.proceed(args);
        System.out.println("宝塔镇河妖");
        return proceed;
    }
}
