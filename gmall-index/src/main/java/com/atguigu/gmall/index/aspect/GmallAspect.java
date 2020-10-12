package com.atguigu.gmall.index.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GmallAspect {

    @Around("@annotation(com.atguigu.gmall.index.annotation.GmallAnnotation)")
    public Object aroundAop(ProceedingJoinPoint joinPoint) throws Throwable {

    }
}
