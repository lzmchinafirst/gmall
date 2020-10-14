package com.atguigu.gmall.index;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GmallIndexApplicationTests {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private RBloomFilter<String> bloomFilter;

    @Test
    void contextLoads() {
        ResponseVo<List<CategoryEntity>> listResponseVo = pmsClient.queryCategoryByParentId(0L);
        List<CategoryEntity> data = listResponseVo.getData();
        for (CategoryEntity datum : data) {
            bloomFilter.add(String.valueOf(datum.getId()));
        }
    }

}
