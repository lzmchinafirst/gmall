package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.interceptor.CartInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThinkService {

    @Autowired
    private CartInterceptor cartInterceptor;
//
//    public List<Cart> queryCarts2() {
//        UserInfo userInfo = cartInterceptor.getUserInfo();
//        Long userId = userInfo.getUserId();
//        String userKeyIndex = userInfo.getUserKey();
//        if(userId == null) {
//            //如果userId为空的话，那么证明了现在并没有登录状态
//            List<Cart> cartList = getUserKeyCarts(userKeyIndex);
//            return cartList;
//        } else {
//            //如果userId不为空的话，那么说明现在已经登录
//            List<Cart> userKeyCarts = getUserKeyCarts(userKeyIndex);
//            String userIdIndex = KEY_PRE + userId;
//            BoundHashOperations<String, Object, Object> idCarts = redisTemplate.boundHashOps(userIdIndex);
//            List<Cart> userIdCarts = idCarts.values().stream().map((value) -> JSON.parseObject(value.toString(), Cart.class)).collect(Collectors.toList());
//            //如果id的购物车为空，那么直接将key的购物车添加
//            if(CollectionUtils.isEmpty(userIdCarts)) {
//                Cart cart = new Cart();
//                //将userkey修改为userid
//                List<Cart> idCartList = userKeyCarts.stream().map((userKeyCart) -> {
//                    userKeyCart.setUserId(userIdIndex);
//                    return userKeyCart;
//                }).collect(Collectors.toList());
//                //向redis中添加数据
//                idCartList.forEach((idCart) -> {idCarts.put(idCart.getUserId(), JSON.toJSONString(idCart));});
//                //删除redis中的旧数据
//                redisTemplate.delete(userKeyIndex);
//                //向mysql中添加数据
//                idCartList.forEach(idCart -> this.cartMapper.insert(idCart));
//                //删除mysql中的旧数据
//                this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id",userKeyIndex));
//            } else {
//                //如果redis中有了userid对应的数据，那么要进行判断比较
//
//            }
//        }
//
//
//        return null;
//    }

}
