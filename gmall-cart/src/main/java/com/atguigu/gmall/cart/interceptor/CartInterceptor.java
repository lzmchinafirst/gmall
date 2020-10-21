package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.cart.properties.JwtProperties;
import com.atguigu.gmall.common.bean.DivException;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class CartInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtProperties jwtProperties;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfo user = new UserInfo();
        //1.首先尝试从cookie中获得user-key
        String userKey = CookieUtils.getCookieValue(request, jwtProperties.getUserKey());
        if(StringUtils.isBlank(userKey)) {
            //2.如果cookie中没有user-key那么将user-key设置到cookie中
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request,response,jwtProperties.getUserKey(),userKey,jwtProperties.getExpire());
        }
        user.setUserKey(userKey);
        //3.尝试从cookie中获得userId
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (StringUtils.isBlank(token)) {
            //4.如果token为空的话，直接放行
            THREAD_LOCAL.set(user);
            return true;
        }
        //5.从token中获得对应的userId
        try {
            Map<String, Object> infoFromToken = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            Integer userId = (Integer) infoFromToken.get("userId");
            user.setUserId(Long.valueOf(userId));
        } catch (Exception e) {
            throw new DivException("您的cookie信息有误！");
        }
        //6.无论结果都要放行
        THREAD_LOCAL.set(user);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //当一切都繁华落尽，是时候安息了
        THREAD_LOCAL.remove();
    }



    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }
}
