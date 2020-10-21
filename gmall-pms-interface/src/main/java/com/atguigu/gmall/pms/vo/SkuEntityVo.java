package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEntityVo extends SkuEntity {
    //sku的类型列表
    private List<SkuAttrValueEntity> saleAttrs;

    //图片
    private List<String> images;

    //积分活动
    private BigDecimal growBounds;
    private BigDecimal buyBounds;

    /**
     * 优惠生效状况1111全满
     * 0-无优惠
     * 1-有优惠
     */
    private List<Integer> work;

    //满减活动
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    //打折活动
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

}
