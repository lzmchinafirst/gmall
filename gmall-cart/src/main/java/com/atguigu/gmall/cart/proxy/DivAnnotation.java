package com.atguigu.gmall.cart.proxy;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DivAnnotation {
    String value() default "";
}
