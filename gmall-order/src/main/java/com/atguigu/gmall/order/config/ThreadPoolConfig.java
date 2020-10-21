package com.atguigu.gmall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
public class ThreadPoolConfig {

    @Value("${thread.pool.core}")
    private Integer core;
    @Value("${thread.pool.max}")
    private Integer max;
    @Value("${thread.pool.alive}")
    private Long alive;
    @Value("${thread.pool.blocking}")
    private Integer blocking;

    @Bean
    public ThreadPoolExecutor init() {
        return new ThreadPoolExecutor(core,max,alive,
                TimeUnit.SECONDS,new ArrayBlockingQueue<>(blocking), Executors.defaultThreadFactory(),
                (a,b) -> log.error("====================FBI WARNING======================="));
    }
}
