package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.PmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("pms-service")
public interface PmsClient extends PmsApi {
}
