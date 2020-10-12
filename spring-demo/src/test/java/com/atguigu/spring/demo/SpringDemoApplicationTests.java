package com.atguigu.spring.demo;

import com.atguigu.spring.demo.bean.User;
import com.atguigu.spring.demo.util.CjlibProxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringDemoApplicationTests {

    @Autowired
    private CjlibProxy<User> proxy;

    @Test
    void contextLoads() {
        User user = this.proxy.getProxy(new User());
        user.say();
    }

}
