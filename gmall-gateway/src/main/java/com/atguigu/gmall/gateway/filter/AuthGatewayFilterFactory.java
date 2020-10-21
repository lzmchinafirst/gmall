package com.atguigu.gmall.gateway.filter;

import com.atguigu.gmall.common.bean.DivException;
import com.atguigu.gmall.common.utils.IpUtil;
import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.gateway.properties.JwtProperties;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.net.HttpHeaders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@EnableConfigurationProperties(JwtProperties.class)
@Slf4j
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthGatewayFilterFactory.MyNameValueConfig> implements Ordered {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("args");
    }


    public AuthGatewayFilterFactory() {
        super(MyNameValueConfig.class);
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    @Override
    public GatewayFilter apply(MyNameValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                //1.判断是否过滤得到的请求路径
                String path = request.getURI().getPath();
                @NotEmpty List<String> args = config.getArgs();
                if (args.stream().allMatch(arg -> path.indexOf(arg) == -1)) {
                    //2.如果请求的路径不在拦截的范围之内，那么直接放行
                    return chain.filter(exchange);
                }
                //3.由于请求有可能通过请求头和cookie的方式获得的toke，因此从这两个地方尝试
                //获得token的值
                String token = request.getHeaders().getFirst("token");
                //4.如果head中为空，那么就到cookie中找
                if (StringUtils.isBlank(token)) {
                    MultiValueMap<String, HttpCookie> cookies = request.getCookies();
                    if (CollectionUtils.isNotEmpty(cookies) && cookies.containsKey(jwtProperties.getCookieName())) {
                        HttpCookie cookie = cookies.getFirst(jwtProperties.getCookieName());
                        token = cookie.getValue();
                    }
                    //5.如果cookie中也没有那么就直接重定向到登录界面
                    if (StringUtils.isBlank(token)) {
                        response.setStatusCode(HttpStatus.SEE_OTHER);
                        response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                        return response.setComplete();
                    }
                }
                try {
                    //6.得到了token的值以后就要对其进行解析
                    Map<String, Object> infoFromToken = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                    String userId = infoFromToken.get("userId").toString();
                    String username = infoFromToken.get("username").toString();
                    //7.对解析出的ip和源头ip进行对比，如果二者相同则放行，如果不同那么抛出异常
                    String ip = infoFromToken.get("ip").toString();
                    String originIp = IpUtil.getIpAddressAtGateway(request);
                    System.out.println("===============ORIGINIP:" + ip + "=================");
                    System.out.println("===============IP:" + ip + "=================");
                    System.out.println("===============USERID:" + userId + "=================");
                    System.out.println("===============USERNAME:" + username + "=================");
                    if (!StringUtils.equals(ip, originIp)) {
                        log.warn("==============FBI WARNING================");
                        throw new DivException("该用户的ip地址有误！");
                    }
                    //8.上述过程都没有问题的话，可以将得到的信息加入到请求头中，随后放行
                    request.mutate().header("userId", userId).build();
                    exchange.mutate().request(request).build();
                    return chain.filter(exchange);
                } catch (Exception e) {
                    //8.如果捕获到了异常，那么直接重定向到登录页面
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://sso.gmall.com/toLogin.html?returnUrl=" + request.getURI());
                    return response.setComplete();
                }
            }
        };
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Validated
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class MyNameValueConfig {
        @NotEmpty
        protected List<String> args;
    }

}
