package com.atguigu.springtask.demo.controller;
import com.atguigu.springtask.demo.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TaskDemo {

    @Autowired
    private TaskService taskService;

    @GetMapping("test")
    @ResponseBody
    public String test() {
        String test = this.taskService.test();
        return test;
    }
}
