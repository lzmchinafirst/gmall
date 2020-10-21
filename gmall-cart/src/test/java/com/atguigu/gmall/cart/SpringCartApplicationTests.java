package com.atguigu.gmall.cart;

import com.atguigu.gmall.cart.interceptor.CartInterceptor;
import com.atguigu.gmall.cart.proxy.Cat;
import com.atguigu.gmall.cart.proxy.ProxyTest2;
import com.atguigu.gmall.cart.proxy.ProxyTest;
import org.aspectj.weaver.ast.Var;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SpringBootTest
class SpringCartApplicationTests {

    @Autowired
    private CartInterceptor interceptor;

    @Autowired
    private ProxyTest2 proxy;

    @Autowired
    private Cat smallCat;

    @Autowired
    private ProxyTest proxyTest;

    @Test
    void contextLoads() {
        this.proxyTest.say();
    }

    @Test
    void test() {
        this.proxyTest.play();
    }

    @Test
    void test2() {
        smallCat.say();
    }

    @Test
    void test3() {
        ThreadLocal<String> threadLocal = new ThreadLocal();
        threadLocal.set("天王盖地虎");
        threadLocal.get();
        System.out.println(threadLocal.get());
        HashMap<String ,String> hashMap = new HashMap<>();
        hashMap.put("haha", "haha");
        hashMap.put("haha","nima");
        System.out.println(hashMap.get("haha"));
        String[] strs = new String[6];
    }

    @Test
    void test4() {
        String[] strs = new String[6];
        strs[0] = "1";
        strs[0] = "2";
        System.out.println(strs[0]);
        Set<String> set = new HashSet<>();
    }
}
