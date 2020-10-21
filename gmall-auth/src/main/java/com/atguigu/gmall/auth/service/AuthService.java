package com.atguigu.gmall.auth.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AuthService {
    void login(HttpServletRequest request, HttpServletResponse response, String loginName, String password, String code);
}
