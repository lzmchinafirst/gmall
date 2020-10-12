package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
public class IndexController {



    @Autowired
    private IndexService indexService;

    //此方法用于跳转到主页
    @GetMapping({"/","/index"})
    public String index(Model model) {
        List<CategoryEntity> data = this.indexService.index();
        model.addAttribute("categories",data);
        return "index";
    }

    //此方法用于根据一级分类的Id获得其对应的二级分类和三级分类的信息
    @GetMapping("index/cates/{cid}")
    @ResponseBody
    public ResponseVo<List<CategoryEntity>> getAllCates(@PathVariable("cid")Long cid) {
        List<CategoryEntity> allCates = this.indexService.getAllCates(cid);
        return ResponseVo.ok(allCates);
    }

    @GetMapping("index/test")
    @ResponseBody
    public ResponseVo test() {
        this.indexService.test();
        return ResponseVo.ok();
    }

    @GetMapping("index/testRead")
    @ResponseBody
    public ResponseVo testRead(){
        this.indexService.testRead();
        return ResponseVo.ok();
    }

    @GetMapping("index/testWrite")
    @ResponseBody
    public ResponseVo testWrite(){
        this.indexService.testWrite();
        return ResponseVo.ok();
    }

    @GetMapping("index/testLatch")
    @ResponseBody
    public ResponseVo testLatch(){
        String latch = this.indexService.testLatch();
        return ResponseVo.ok(latch);
    }

    @GetMapping("index/testCountDown")
    @ResponseBody
    public ResponseVo testCountDown(){
        String countDown = this.indexService.testCountDown();
        return ResponseVo.ok(countDown);
    }

}
