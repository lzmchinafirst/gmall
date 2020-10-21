package com.atguigu.gmall.cart.proxy;


import org.springframework.stereotype.Component;

@Component
public class ProxyTest2 implements Runnable{


    @Override
    @DivAnnotation
    public void run() {
        System.out.println("im a good boy");
    }
}
