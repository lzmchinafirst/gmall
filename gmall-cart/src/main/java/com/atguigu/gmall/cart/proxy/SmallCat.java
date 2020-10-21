package com.atguigu.gmall.cart.proxy;

import org.springframework.stereotype.Component;

@Component
public class SmallCat implements Cat{
    @Override
    @DivAnnotation
    public void say() {
        System.out.println("im a small cat");
    }
}
