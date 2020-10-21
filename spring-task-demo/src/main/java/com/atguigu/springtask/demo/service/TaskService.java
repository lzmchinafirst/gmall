package com.atguigu.springtask.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class TaskService {

    @Autowired
    private TaskServiceAsyn serviceAsyn;

    public String test() {
        long now = System.currentTimeMillis();
        Future<String> stringFuture = this.serviceAsyn.execute1();
        ListenableFuture<String> stringListenableFuture = this.serviceAsyn.execute2();
        try {
            stringFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        stringListenableFuture.addCallback((success) -> {
            System.out.println(success);
        },(ex) ->{
            System.out.println(ex.getMessage());
        });
        System.out.println("============================");
        this.serviceAsyn.execute2();
        System.out.println(System.currentTimeMillis() - now);
        return "fuck you trump";
    }
}
