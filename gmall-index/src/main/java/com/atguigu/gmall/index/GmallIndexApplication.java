package com.atguigu.gmall.index;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableFeignClients
public class GmallIndexApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallIndexApplication.class, args);
    }

}
