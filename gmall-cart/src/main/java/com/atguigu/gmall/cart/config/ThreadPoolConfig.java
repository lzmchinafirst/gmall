package com.atguigu.gmall.cart.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
@Slf4j
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor init() {
        return new ThreadPoolExecutor(100,500,60, TimeUnit.SECONDS,new ArrayBlockingQueue<>(1000),
                Executors.defaultThreadFactory(),
                (Runnable r, ThreadPoolExecutor executor) -> {log.warn("================FBI WARNING:IT'S TOO LOT====================");});
    }

}
