package com.atguigu.gmall.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.auth.feign.GmallUmsClient;
import com.atguigu.gmall.auth.properties.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import com.atguigu.gmall.common.bean.DivException;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthServiceImpl implements AuthService {

    @Autowired
    private GmallUmsClient umsClient;

    @Autowired
    JwtProperties jwtProperties;

    @Override
    public void login(HttpServletRequest request, HttpServletResponse response, String loginName, String password, String code) {
        try {
            //1.根据上传的登录信息获得具体的用户信息
            ResponseVo<String> stringResponseVo = this.umsClient.queryUser(loginName, password);
            String data = stringResponseVo.getData();
            UserEntity userEntity = JSON.parseObject(data, UserEntity.class);
            if(userEntity == null) {
                throw new DivException("您输入的用户名或者密码有误!");
            }
            //2.将获得的用户信息封装成一个map
            Map<String,Object> map = new HashMap<>();
            //2.1将用户的id封装进map中
            map.put("userId",userEntity.getId());
            //2.2将用户的用户名封装进入map中
            map.put("username",userEntity.getUsername());
            //2.3为了安全我们将主机的ip地址封装到map中
            map.put("ip", IpUtil.getIpAddressAtService(request));
            //3.将整个map封装成为一个jwt
            String jwt = JwtUtils.generateToken(map, jwtProperties.getPrivateKey(),
                    jwtProperties.getExpire());
            //4.向浏览器的cookie中添加token信息
            CookieUtils.setCookie(request,response, jwtProperties.getCookieName(),jwt,
                    jwtProperties.getExpire() * 60);
            //5.将用户的昵称封装到cookie中
            CookieUtils.setCookie(request,response, jwtProperties.getUnick(),
                    userEntity.getNickname(), jwtProperties.getExpire() * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
