package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.CategoryEntity;

import java.util.List;

public interface IndexService {
    public List<CategoryEntity> index();
    public List<CategoryEntity> getAllCates(Long cid);
    public void test();
    public void testRead();
    public void testWrite();
    public String testLatch();
    public String testCountDown();
}
