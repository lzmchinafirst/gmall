package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.UmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ums-service")
public interface GmallUmsClient extends UmsApi {
}
