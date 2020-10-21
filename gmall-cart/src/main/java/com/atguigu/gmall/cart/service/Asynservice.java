package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.List;
import java.util.logging.Filter;

@Service
public class Asynservice {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void deleteCartByUserId(String userKeyKey) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userKeyKey));
    }

    @Async
    public void insertCarts(List<Cart> loginCarts) {
        loginCarts.forEach((loginCart) -> {
            this.cartMapper.insert(loginCart);
        });
    }

    @Async
    public void insertCart(Cart unLockCart) {
        this.cartMapper.insert(unLockCart);
    }

    @Async
    public void updataCart(Cart loginCart) {
        this.cartMapper.update(loginCart,new QueryWrapper<Cart>().eq("sku_id",loginCart.getSkuId()));
    }

    @Async
    public void deleteCartByUserIdAndSkuId(String userId, Long skuId) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userId).eq("sku_id",skuId));
    }

    @Async
    public void updateCart(Cart newCart,Long skuId) {
        this.cartMapper.update(newCart,new QueryWrapper<Cart>().eq("sku_id",skuId));

    }
}
