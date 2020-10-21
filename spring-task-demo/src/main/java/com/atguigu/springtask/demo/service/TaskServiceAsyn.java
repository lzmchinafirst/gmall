package com.atguigu.springtask.demo.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class TaskServiceAsyn {

    @Async
    public Future<String> execute1() {
        System.out.println("hello, world");
        try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace();}
        System.out.println("hello, world end");
        return AsyncResult.forValue("hello word end end");
    }

    @Async
    public ListenableFuture<String> execute2(){
        try {
            System.out.println("hello trump");
            try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace();}
            System.out.println("hello trump end");
        } catch (Exception e) {
            AsyncResult.forExecutionException(new RuntimeException(e.getMessage()));
        }
        return AsyncResult.forValue("hello trump end end");
    }
}
