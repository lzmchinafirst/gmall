package com.atguigu.gmall.index.utils;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilter {

    @Autowired
    private RedissonClient redission;

    @Bean
    public RBloomFilter getBloomFilter() {
        RBloomFilter<String> bloomFilter = redission.getBloomFilter("category:id");
        // 初始化布隆过滤器，预计统计元素数量为50，期望误差率为0.03
        bloomFilter.tryInit(50L, 0.03);
        return bloomFilter;
    }
}
