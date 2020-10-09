package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface PmsApi {

    //根据id查询对应的spu信息
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);
    //查询spu
    @PostMapping("pms/spu/page/json")
    public ResponseVo<List<SpuEntity>> querySpuJson(@RequestBody PageParamVo paramVo);

    //根据spu的id以及categoryId查询对应的sku_attr_value
    @GetMapping("pms/spuattrvalue/spu/{spuId}/{categoryId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySpuAttrValueBySpuIdAndCategoryId(@PathVariable("spuId")Long spuId,
                                                                                      @PathVariable("categoryId")Long categoryId);
    //根据sku的id以及categoryId查询对应的sku_attr_value
    @GetMapping("pms/skuattrvalue/sku/{skuId}/{categoryId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuAttrByCategoryIdAndSkuId(@PathVariable("skuId")Long skuId,
                                                                                 @PathVariable("categoryId")Long categoryId);

    //根据类别的id查询类别信息
    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //根据品牌的id查询具体的品牌信息
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //根据spu的id查询对应的所有的sku的信息
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> queryAllSkuInItSpu(@PathVariable("spuId")Long spuId);
}
