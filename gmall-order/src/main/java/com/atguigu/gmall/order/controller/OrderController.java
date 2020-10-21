package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.vo.OrderSubmitVo;
import com.atguigu.gmall.order.Vo.OrderConfirmVo;
import com.atguigu.gmall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("submit")
    public ResponseVo<String> submitOrder(@RequestBody OrderSubmitVo orderSubmitVo) {
        OrderEntity orderEntity = this.orderService.submitOrder(orderSubmitVo);
        if(orderEntity == null) {
            throw new OrderException("服务器异常！");
        }
        return ResponseVo.ok(orderEntity.getOrderSn());
    }


    /**
     * 回显订单结算页的数据
     * @return
     */
    @GetMapping("confirm")
    public String confirm(Model model) {
        OrderConfirmVo orderConfirmVo = this.orderService.confirm();
        model.addAttribute("confirmVo",orderConfirmVo);
        return "trade";
    }
}
