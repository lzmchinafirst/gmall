package com.atguigu.springtask.demo.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

@Configuration
public class AsynUnCaughtConfig implements AsyncConfigurer {

    @Autowired
    private AsynUnCaughtExceptionHandler unCaughtExceptionHandler;
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return unCaughtExceptionHandler;
    }
}
