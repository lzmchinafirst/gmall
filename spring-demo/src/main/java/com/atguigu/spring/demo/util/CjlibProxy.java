package com.atguigu.spring.demo.util;


import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class CjlibProxy<T> {

    public T getProxy(Object target) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                //执行业务
                System.out.println("请叫我峰哥");
                Object result = methodProxy.invokeSuper(o, objects);
                //执行业务
                System.out.println("做好事不留名");
                return result;
            }
        });
        T result = (T) enhancer.create();
        return result;
    }
}
