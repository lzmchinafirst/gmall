package com.atguigu.gmall.item.config;

import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Autowired
    private DevRejectExcutionHandler devRejectExcutionHandler;

    @Bean
    public ThreadPoolExecutor getThreadPool() {
        return new ThreadPoolExecutor(100,500,60,
                TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000),
                Executors.defaultThreadFactory(),devRejectExcutionHandler);
    }
}
