package com.atguigu.gmall.cart.proxy;


import org.springframework.stereotype.Component;

@Component
public class ProxyTest {
    private Integer age;
    private String name;


    public ProxyTest(Integer age, String name) {
        this.age = age;
        this.name = name;
    }


    public ProxyTest() {
    }

    public void say() {
        System.out.println("i'm " + age + " years ols");
        play();
    }

    @DivAnnotation
    public void play() {
        System.out.println("hello, my name is " + name);
    }
}
