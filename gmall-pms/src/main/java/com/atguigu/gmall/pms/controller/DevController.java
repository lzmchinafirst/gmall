package com.atguigu.gmall.pms.controller;

import com.atguigu.gmall.pms.entity.SpuDescEntity;
import com.atguigu.gmall.pms.feign.SmsApiClient;
import com.atguigu.gmall.pms.service.SpuDescService;
import com.atguigu.gmall.sms.vo.SkuBoundsEntity;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("pms/dev")
@Api("自定义的controller")
public class DevController {
    @Autowired
    private SpuDescService spuDescService;
    @Autowired
    private SmsApiClient smsApiClient;

    @Transactional
//    @GlobalTransactional
    @GetMapping("insert")
    public void insert() {
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        spuDescEntity.setSpuId(250L);
        spuDescEntity.setDecript("fuckyoutrump");
        spuDescService.save(spuDescEntity);
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(250L);
        skuBoundsEntity.setBuyBounds(new BigDecimal(2000));
        skuBoundsEntity.setGrowBounds(new BigDecimal(2000));
        skuBoundsEntity.setWork(1);
        smsApiClient.insert(skuBoundsEntity);
//        int a = 10 / 0;
    }
}
