package com.atguigu.gmall.index.annotation;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface GmallAnnotation {
    //锁的前缀
    String preFix() default "lock:";
    //锁的时间
    String lockTime() default "5";
    //锁的时间单位
    TimeUnit LOCK_TIME_UNIT() default TimeUnit.SECONDS;
    //保存的时间
    String saveTime() default "30";
    //保存的时间单位
    TimeUnit SAVE_TIME_UNIT() default TimeUnit.DAYS;
    //保存的变动时间范围
    String changeTimeBound() default "5";
    //临时保存时间
    String shortSave() default "5";
    //临时保存的时间单位
    TimeUnit SHORT_TIME_UNIT() default TimeUnit.MINUTES;
}
