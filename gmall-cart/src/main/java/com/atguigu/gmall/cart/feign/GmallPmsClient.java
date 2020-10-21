package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.PmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface GmallPmsClient extends PmsApi {
}
