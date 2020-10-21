package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private ThreadPoolExecutor threadPool;


    @Override
    public ItemVo load(Long skuId) {

        ItemVo itemVo = new ItemVo();

        //1.根据skuid查询对应的sku
        CompletableFuture<SkuEntity> skuEntityCompletableFuture = CompletableFuture.supplyAsync(() -> {
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuId);
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            System.out.println(skuEntity.getDefaultImage());
            return skuEntity;
        }, threadPool);
        //2.根据cid3查询分类信息
        CompletableFuture<Void> categoryCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync((skuEntity -> {
            ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryAllCategoriesByLv3Id(skuEntity.getCatagoryId());
            List<CategoryEntity> categoryEntityList = listResponseVo.getData();
            itemVo.setCategories(categoryEntityList);
        }), threadPool);
        //3.根据品牌的id查询对应的品牌
        CompletableFuture<Void> brandCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync((skuEntity -> {
            Long brandId = skuEntity.getBrandId();
            ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(brandId);
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }), threadPool);
        //4.根据spuid查询对应的spu
        CompletableFuture<Void> spuCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync((skuEntity -> {
            ResponseVo<SpuEntity> spuEntityResponseVo = this.pmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuId(spuEntity.getId());
                itemVo.setSpuName(spuEntity.getName());
            }
        }), threadPool);
        //5.根据skuid查询图片
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuImagesEntity>> imgResponseVo = this.pmsClient.queryAllImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = imgResponseVo.getData();
            itemVo.setImages(skuImagesEntities);
        }, threadPool);
        //6.根据skuId查询sku的营销信息
        CompletableFuture<Void> skuSaleCompletableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<ItemSaleVo>> saleStrategy = this.smsClient.queryAllSaleStrategy(skuId);
            List<ItemSaleVo> saleStrategyData = saleStrategy.getData();
            itemVo.setSales(saleStrategyData);
        });
        //7.根据skuId查询sku的库存信息
        CompletableFuture<Void> storeComplatableFuture = CompletableFuture.runAsync(() -> {
            ResponseVo<List<WareSkuEntity>> wareSkuByItId = this.wmsClient.queryWareSkuByItId(skuId);
            List<WareSkuEntity> wareSkuEntityList = wareSkuByItId.getData();
            if (CollectionUtils.isNotEmpty(wareSkuEntityList)) {
                itemVo.setStore(wareSkuEntityList.stream().anyMatch((wareSkuEntity) -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPool);
        //8.根据spuId查询spu下的所有sku的销售属性
        CompletableFuture<Void> skuSaleBySpuId = skuEntityCompletableFuture.thenAcceptAsync((skuEntity) -> {
            ResponseVo<List<SaleAttrValueVo>> saleAttrValueVoResponseVo = this.pmsClient.queryAllSaleElementsBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> saleAttrValueVos = saleAttrValueVoResponseVo.getData();
            itemVo.setSaleAttrs(saleAttrValueVos);
        }, threadPool);
        //9.当前sku的销售属性
        CompletableFuture<Void> skuSaleNow = CompletableFuture.runAsync(() -> {
            ResponseVo<List<SkuAttrValueEntity>> queryAllSalesBySkuId = this.pmsClient.queryAllSalesBySkuId(skuId);
            List<SkuAttrValueEntity> skuIdData = queryAllSalesBySkuId.getData();
            if (skuIdData != null) {
                Map<Long, String> attrMap = skuIdData.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue));
                itemVo.setSaleAttr(attrMap);
            }
        }, threadPool);
        //10.根据spuId查询spu下的所有的sku以及销售属性的映射关系
        CompletableFuture<Void> mappingCompletableFuture = skuEntityCompletableFuture.thenAcceptAsync((skuEntity) -> {
            ResponseVo<String> saleAndSkuRelationshap = this.pmsClient.getSaleAndSkuRelationshap(skuEntity.getSpuId());
            String skusJson = saleAndSkuRelationshap.getData();
            itemVo.setSkuJsons(skusJson);
        }, threadPool);
        //11.根据spuId查询spu的海报信息
        CompletableFuture<Void> SeaLetter = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.pmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null && StringUtils.isNotBlank(spuDescEntity.getDecript())) {
                String[] images = StringUtils.split(spuDescEntity.getDecript(), ",");
                itemVo.setSpuImages(Arrays.asList(images));
            }
        }, threadPool);
        //12.根据cid3 spuid skuid查询组以及组下的规格参数
        CompletableFuture<Void> group = skuEntityCompletableFuture.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> groupsBySpuIdAndCid = this.pmsClient.queryGroupsBySpuIdAndCid(skuEntity.getSpuId(), skuId, skuEntity.getCatagoryId());
            List<ItemGroupVo> itemGroupVos = groupsBySpuIdAndCid.getData();
            itemVo.setGroups(itemGroupVos);
        }, threadPool);
        CompletableFuture.allOf(skuEntityCompletableFuture,categoryCompletableFuture,brandCompletableFuture,spuCompletableFuture,
                skuSaleBySpuId, mappingCompletableFuture,SeaLetter,group
                ,skuSaleNow,imageCompletableFuture,
                skuSaleCompletableFuture,storeComplatableFuture).join();
        return itemVo;
    }
}
