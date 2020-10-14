package com.atguigu.gmall.sms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuBoundsEntity;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface SmsApiInterface {
    @PostMapping("sms/skubounds/skusale/save")
    public ResponseVo skuSave(@RequestBody SkuSaleVo skuSaleVo);

    @PostMapping("sms/dev/insert")
    public void insert(@RequestBody SkuBoundsEntity skuBoundsEntity);

    //根据skuid查询所有的营销信息
    @GetMapping("sms/skubounds/sku/{skuid}")
    public ResponseVo<List<ItemSaleVo>> queryAllSaleStrategy(@PathVariable("skuid")Long skuid);

}
