package com.atguigu.gmall.sms.controller;

import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("sms/dev")
public class DevController {

    @Autowired
    private SkuBoundsService skuBoundsService;

    @Transactional
    @PostMapping("insert")
    public void insert(@RequestBody SkuBoundsEntity skuBoundsEntity) {
        skuBoundsService.save(skuBoundsEntity);
        int a = 10 / 0;
    }
}
