package com.atguigu.gmall.payment.config;

import com.atguigu.gmall.payment.interceptor.PaymentInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PaymentInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private PaymentInterceptor paymentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(paymentInterceptor).addPathPatterns("/**");
    }
}
