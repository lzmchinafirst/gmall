package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

@Data
public class SpuEntityVo extends SpuEntity {
    private List<String> spuImages;
    private List<SpuAttrValueEntityVo> baseAttrs;
    private List<SkuEntityVo> skus;
}
