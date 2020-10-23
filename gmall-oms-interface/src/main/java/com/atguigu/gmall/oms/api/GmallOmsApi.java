package com.atguigu.gmall.oms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface GmallOmsApi {

    //保存订单以及订单的详情
    @PostMapping("oms/order/{userId}")
    public ResponseVo<OrderEntity> saveOrder(@RequestBody OrderSubmitVo orderSubmitVo, @PathVariable("userId")Long userId);

    //根据订单的编号查询订单
    @GetMapping("oms/order/token/{orderSn}")
    public ResponseVo<OrderEntity> getOrder(@PathVariable("orderSn")String orderToken);
}