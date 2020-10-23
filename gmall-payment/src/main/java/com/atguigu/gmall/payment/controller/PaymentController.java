package com.atguigu.gmall.payment.controller;

import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.payment.entity.UserInfo;
import com.atguigu.gmall.payment.interceptor.PaymentInterceptor;
import com.atguigu.gmall.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    //跳转到支付界面
    @GetMapping("pay.html")
    public String toPay(@RequestParam("orderToken")String orderToken, Model model) {
        OrderEntity orderEntity = this.paymentService.queryOrderByOrderToken(orderToken);
        UserInfo userInfo = PaymentInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if(orderEntity == null || orderEntity.getUserId() != userId || orderEntity.getStatus() != 0) {
            throw new OrderException("订单异常");
        }
        model.addAttribute("orderEntity",orderEntity);
        return "pay";
    }

    @GetMapping("alipay.html")
    @ResponseBody
    public String toAlipay(@RequestParam("orderToken")String orderToken) {
        OrderEntity orderEntity = this.paymentService.queryOrderByOrderToken(orderToken);
        UserInfo userInfo = PaymentInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        if(orderEntity == null || orderEntity.getUserId() != userId || orderEntity.getStatus() != 0) {
            throw new OrderException("订单异常");

        }

    }

}
