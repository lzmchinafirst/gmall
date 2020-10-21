package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.CartInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.bean.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Api(description = "购物车模块")
public class CartController {

    @Autowired
    private CartInterceptor interceptor;

    @Autowired
    private CartService cartService;


    @GetMapping("check/{userId}")
    public ResponseVo<List<Cart>> queryCartsSelected(@PathVariable("userId")Long userId) {
        List<Cart> cartList = this.cartService.queryCartsSelected(userId);
        return ResponseVo.ok(cartList);
    }

    @GetMapping
    @ApiOperation("跳转到添加购物车模块")
    public String redirectAddCart(Cart cart) {
        this.cartService.addCart(cart);
        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId();
    }

    @GetMapping("addCart.html")
    @ApiOperation("添加购物车")
    public String addCart(@RequestParam("skuId")Long skuId, Model model) {
        Cart cart = this.cartService.querySkuBySkuId(skuId);
        model.addAttribute("cart",cart);
        return "addCart";
    }

    @GetMapping("cart.html")
    public String queryCart(Model model) {
        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts",carts);
        return "cart";
    }

    @PostMapping("updateNum")
    public ResponseVo updateNum(@RequestBody Cart cart) {
        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    public ResponseVo deleteCartBySkuId(@RequestParam("skuId")Long skuId) {
        this.cartService.deleteCartBySkuId(skuId);
        return ResponseVo.ok();
    }

}
