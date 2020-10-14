package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface PmsApi {

    //根据分类id，spuid以及skuid查询分组以及组下的规格参数值
    @GetMapping("pms/attrgroup/withattrvalues")
    public ResponseVo<List<ItemGroupVo>> queryGroupsBySpuIdAndCid(
            @RequestParam("spuId")Long spuId,
            @RequestParam("skuId")Long skuId,
            @RequestParam("cid")Long cid
    );

    //根据skuId查询sku
    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    //根据sku中的spuId查询商铺的描述信息
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

    //根据sku中的三级分类id查询一二三级分类
    @GetMapping("pms/category/getAll/{cid3}")
    public ResponseVo<List<CategoryEntity>> queryAllCategoriesByLv3Id(@PathVariable("cid3")Long cid3);

    //根据pid查询对应的类别的集合
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoryByParentId(@PathVariable("parentId")Long parentId);

    //根据分类的一级id查询其对应的所有的分类信息
    @GetMapping("pms/category/getCategories/{cid}")
    public ResponseVo<List<CategoryEntity>> queryCategories(@PathVariable("cid")Long cid);

    //根据品牌的id查询具体的品牌信息
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //根据spu的id查询对应的所有的sku的信息
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> queryAllSkuInItSpu(@PathVariable("spuId")Long spuId);

    //根据skuid查询sku所有的图片
    @GetMapping("pms/skuimages/getImgs/{skuid}")
    public ResponseVo<List<SkuImagesEntity>>  queryAllImagesBySkuId(@PathVariable("skuid")Long skuid);

    //根据sku中的spuid查询spu下的所有的销售属性
    @GetMapping("pms/skuattrvalue/sale/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> queryAllSaleElementsBySpuId(@PathVariable("spuId")Long spuId);

   //根据id查询销售属性
    @GetMapping("pms/skuattrvalue/{id}")
    public ResponseVo<SkuAttrValueEntity> querySkuAttrValueById(@PathVariable("id") Long id);

    //根据skuId查询当前sku的销售属性
    @GetMapping("pms/skuattrvalue/getThis/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> queryAllSalesBySkuId(@PathVariable("skuId")Long skuId);

    //根据sku中的spuid查询spu下的所有的sku，销售属性组合与skuid的映射关系
    @GetMapping("pms/skuattrvalue/relationshap/{spuId}")
    public ResponseVo<String> getSaleAndSkuRelationshap(@PathVariable("spuId")Long spuId);

    //根据spuId查询spu的海报信息
    @GetMapping("pms/spudesc/{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);
}
