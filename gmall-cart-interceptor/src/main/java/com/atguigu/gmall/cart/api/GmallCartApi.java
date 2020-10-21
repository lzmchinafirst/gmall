package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallCartApi {

    //获取登录用户勾选的购物车
    @GetMapping("check/{userId}")
    public ResponseVo<List<Cart>> queryCartsSelected(@PathVariable("userId")Long userId);
}
