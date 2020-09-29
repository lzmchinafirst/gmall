package com.atguigu.gmall.search.data;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.PmsClient;
import com.atguigu.gmall.search.feign.WmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.SearchRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class SearchBegin {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private WmsClient wmsClient;

    /**
     * 创建索引和类型
     */
    @Test
    void createIndex() {
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);
    }

    @Test
    void insertDate() {
        Integer pageNum = 1;
        Integer pageSize = 100;
        do {
            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            ResponseVo<List<SpuEntity>> responseVo =
                    this.pmsClient.querySpuJson(pageParamVo);
            List<SpuEntity> spus = responseVo.getData();
            System.out.println("spus = " + spus);
            if(CollectionUtils.isEmpty(spus)) {
                continue;
            }
            spus.forEach(spu -> {
                Long categoryId = spu.getCategoryId();
                //根据spu的id查询对应的sku集合，并将其转化为Goods对象
                Long spuId = spu.getId();
                ResponseVo<List<SkuEntity>> response = pmsClient.queryAllSkuInItSpu(spuId);
                List<SkuEntity> skuList = response.getData();
                List<Goods> goodsList = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(skuList)) {
                        goodsList = skuList.stream().map(sku -> {
                        Goods goods = new Goods();
                        goods.setSkuId(sku.getId());
                        goods.setTitle(sku.getTitle());
                        goods.setSubTitle(sku.getSubtitle());
                        goods.setDefaultImage(sku.getDefaultImage());
                        goods.setPrice(sku.getPrice().doubleValue());
                        //设置品牌信息
                        Long brandId = sku.getBrandId();
                        ResponseVo<BrandEntity> brandResponse = this.pmsClient.queryBrandById(brandId);
                        BrandEntity brand = brandResponse.getData();
                        if (brand != null) {
                            goods.setBrandId(brand.getId());
                            goods.setBrandName(brand.getName());
                            goods.setLogo(brand.getLogo());
                        }
                        //分类相关信息
                        ResponseVo<CategoryEntity> categoryResponse = this.pmsClient.queryCategoryById(categoryId);
                        CategoryEntity categoryResponseData = categoryResponse.getData();
                        if (categoryResponseData != null) {
                            goods.setCategoryId(categoryResponseData.getId());
                            goods.setCategoryName(categoryResponseData.getName());
                        }
                        //spu相关信息
                        goods.setCreateTime(spu.getCreateTime());
                        //库存相关信息
                        ResponseVo<List<WareSkuEntity>> wareResponse = this.wmsClient.queryWareSkuByItId(sku.getId());
                        List<WareSkuEntity> wareList = wareResponse.getData();
                        if (CollectionUtils.isNotEmpty(wareList)) {
                            goods.setStore(wareList.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                            goods.setSales(wareList.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                        }
                        //关于sku的检索属性
                        List<SearchAttrValue> list = new ArrayList<>();
                        ResponseVo<List<SkuAttrValueEntity>> skuAttrResponse = this.pmsClient.querySkuAttrByCategoryIdAndSkuId(sku.getId(), categoryId);
                        List<SkuAttrValueEntity> skuAttrValueEntityList = skuAttrResponse.getData();
                        if (CollectionUtils.isNotEmpty(skuAttrValueEntityList)) {
                            list.addAll(skuAttrValueEntityList.stream().map(skuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList()));
                        }
                        //关于spu的检索属性
                        ResponseVo<List<SpuAttrValueEntity>> spuAttrResponse = this.pmsClient.querySpuAttrValueBySpuIdAndCategoryId(spuId, categoryId);
                        List<SpuAttrValueEntity> spuAttrValueEntityList = spuAttrResponse.getData();
                        if (CollectionUtils.isNotEmpty(spuAttrValueEntityList)) {
                            list.addAll(spuAttrValueEntityList.stream().map(spuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList()));
                        }
                        goods.setSearchAttrs(list);
                        return goods;
                    }).collect(Collectors.toList());
                }
                searchRepository.saveAll(goodsList);
            });
            pageSize = spus.size();
            pageNum++;
        } while (pageSize == 100);
    }
}
