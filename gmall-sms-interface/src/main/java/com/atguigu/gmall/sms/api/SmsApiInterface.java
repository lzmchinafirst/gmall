package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface SmsApiInterface {
    @PostMapping("sms/skubounds/skusale/save")
    public ResponseVo skuSave(@RequestBody SkuSaleVo skuSaleVo);
}
