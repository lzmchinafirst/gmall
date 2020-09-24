package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.SmsApiInterface;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("sms-service")
public interface SmsApiClient extends SmsApiInterface {
}
