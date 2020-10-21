package com.atguigu.gmall.order.interceptor;

import com.atguigu.gmall.order.pojo.UserInfo;
import org.omg.PortableInterceptor.Interceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.standard.processor.StandardInliningCDATASectionProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class OrderInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    //拦截器拦截登录状态后的请求头中的token，随后从token中获得登录信息
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("userId");
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(Long.valueOf(userId));
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    //清空threadlocal，防止发生内存泄漏
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        THREAD_LOCAL.remove();
    }

    //获取当前线程中的userinfo
    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }
}
