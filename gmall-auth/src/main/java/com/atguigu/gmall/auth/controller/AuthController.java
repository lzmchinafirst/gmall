package com.atguigu.gmall.auth.controller;

import com.atguigu.gmall.auth.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Api(description = "认证中心")
public class AuthController {

    @Autowired
    private AuthService authService;

    @ApiOperation("跳转到用户登录界面")
    @GetMapping("toLogin.html")
    public String loginWindow(@RequestParam(value = "returnUrl",required = false)String returnUrl, Model model) {
        //添加返回地址
        model.addAttribute("returnUrl",returnUrl);
        return "login";
    }

    @ApiOperation("提交登录信息")
    @PostMapping("login")
    public String login(HttpServletRequest request,
                        HttpServletResponse response,
                        @RequestParam("loginName")String loginName,
                        @RequestParam("password")String password,
                        @RequestParam(value = "code",required = false)String code,
                        @RequestParam("returnUrl")String returnUrl) {
        this.authService.login(request,response,loginName,password,code);
        return "redirect:" + returnUrl;
    }
}
