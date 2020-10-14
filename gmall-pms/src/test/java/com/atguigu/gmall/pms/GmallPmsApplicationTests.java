package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallPmsApplicationTests {

    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Test
    void contextLoads() {
        System.out.println(skuAttrValueService.getSaleAndSkuRelationshap(7L));
    }

}
