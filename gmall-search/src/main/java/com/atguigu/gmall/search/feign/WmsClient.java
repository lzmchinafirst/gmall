package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.WmsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("wms-service")
public interface WmsClient extends WmsApi {
}
