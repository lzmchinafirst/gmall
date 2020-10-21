package com.atguigu.gmall.cart;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.atguigu.gmall.cart.mapper")
@EnableFeignClients
public class SpringCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCartApplication.class, args);
    }

}
