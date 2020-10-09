package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.PmsClient;
import com.atguigu.gmall.search.feign.WmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.SearchRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PmsListener {

    @Autowired
    private PmsClient pmsClient;

    @Autowired
    private SearchRepository searchRepository;

    @Autowired
    private WmsClient wmsClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "PMS_INSERT_QUEUE", durable = "true"),
            exchange = @Exchange(value = "PMS_ITEM_EXCAHNG", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"ITEM.INSERT"}
    ))
    public void listener(Long spuId, Channel channel, Message message) throws IOException {

        try {
            //实现大保存业务
            this.bigSave(spuId);
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
            // 是否已经重试过
            if (message.getMessageProperties().getRedelivered()) {
                // 已重试过直接拒绝
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } else {
                // 未重试过，重新入队
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
        }
    }

    public void bigSave(Long spuid) {
        ResponseVo<SpuEntity> spuEntityResponseVo = pmsClient.querySpuById(spuid);
        SpuEntity spu = spuEntityResponseVo.getData();
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
    }
}
