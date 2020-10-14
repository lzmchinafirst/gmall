package com.atguigu.gmall.index.annotation;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface GmallCache {
    String cachePreFix() default "";
    String cacheTime() default "5";
    TimeUnit TIME_UNIT () default TimeUnit.DAYS;
    String random () default "5";
    String locPreFix() default "lock:";

}
