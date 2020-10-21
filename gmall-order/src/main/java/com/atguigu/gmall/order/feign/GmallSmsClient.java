package com.atguigu.gmall.order.feign;

import com.atguigu.gmall.sms.api.SmsApiInterface;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface GmallSmsClient extends SmsApiInterface {
}
