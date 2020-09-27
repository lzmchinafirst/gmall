package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.SmsApiClient;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SkuEntityVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueEntityVo;
import com.atguigu.gmall.pms.vo.SpuEntityVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.transaction.annotation.Transactional;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper spuDescMapper;

    @Autowired
    private SpuAttrValueService spuAttrValueService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapperp;

    @Autowired
    private SmsApiClient smsApiClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCategoryId(Long cid, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (cid != 0) {
            wrapper.eq("category_id",cid);
        }
        String key = pageParamVo.getKey();
        if(StringUtils.isNotBlank(key)) {
            wrapper.and(a -> {
                a.like("id",key).or().like("name",key);
            });
        }
        IPage<SpuEntity> page = this.page(pageParamVo.getPage(), wrapper);
        return new PageResultVo(page);
    }

    /**
     * 保存上传的SpuEntityVo对象
     * @param spuVo
     */
    @Override
    @GlobalTransactional
    public void saveSpuEntityVo(SpuEntityVo spuVo) {
        //第一步：保存数据到pms_spu
        Long spuId = saveSpu(spuVo);
        //第二步：保存数据到pms_spu_desc
        saveSpuDesc(spuVo, spuId);
        //第三步：保存数据到pms_spu_attr_value
        saveSpuAttr(spuVo, spuId);
        //第四步：保存数据到pms_sku
        saveSku(spuVo, spuId);
        //int a = 10 / 0;
    }

    private void saveSku(SpuEntityVo spuVo, Long spuId) {
        List<SkuEntityVo> skus = spuVo.getSkus();
        if(CollectionUtils.isEmpty(skus)) {
            return;
        }
        skus.forEach(a -> {
            SkuEntity skuEntity = new SkuEntity();
            BeanUtils.copyProperties(a,skuEntity);
            skuEntity.setSpuId(spuId);
            skuEntity.setCatagoryId(spuVo.getCategoryId());
            skuEntity.setBrandId(spuVo.getBrandId());
            List<String> images = a.getImages();
            if(CollectionUtils.isNotEmpty(images)) {
                skuEntity.setDefaultImage(a.getDefaultImage() == null ? images.get(0) : a.getDefaultImage());
            }
            this.skuMapper.insert(skuEntity);
            Long skuId = skuEntity.getId();
            //第五步：保存数据到pms_sku_images
            saveSkuImages(skuEntity, images, skuId);
            //第六步：保存数据到pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = a.getSaleAttrs();
            if(CollectionUtils.isEmpty(saleAttrs)) {
                return;
            }
            saleAttrs.forEach(saleAttr -> {
                saleAttr.setSkuId(skuId);
                saleAttr.setSort(0);
                skuAttrValueMapperp.insert(saleAttr);
            });
            //第七步：向数据库中添加sku对应的优惠信息
            saveSkuSms(a, skuId);
        });
    }

    private void saveSkuSms(SkuEntityVo a, Long skuId) {
        SkuSaleVo skuSaleVo = new SkuSaleVo();
        BeanUtils.copyProperties(a,skuSaleVo);
        skuSaleVo.setSkuId(skuId);
        smsApiClient.skuSave(skuSaleVo);
    }

    private void saveSkuImages(SkuEntity skuEntity, List<String> images, Long skuId) {
        if(CollectionUtils.isNotEmpty(images)) {
            List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setSkuId(skuId);
                skuImagesEntity.setUrl(image);
                skuImagesEntity.setSort(0);
                skuImagesEntity.setDefaultStatus(image.equals(skuEntity.getDefaultImage()) ? 1 : 0);
                return skuImagesEntity;
            }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntities);
        }
    }

    private void saveSpuAttr(SpuEntityVo spuVo, Long spuId) {
        List<SpuAttrValueEntityVo> baseAttrs = spuVo.getBaseAttrs();
        List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueEntityVo -> {
            SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
            spuAttrValueEntity.setSpuId(spuId);
            spuAttrValueEntity.setAttrId(spuAttrValueEntityVo.getAttrId());
            spuAttrValueEntity.setAttrName(spuAttrValueEntityVo.getAttrName());
            spuAttrValueEntity.setAttrValue(spuAttrValueEntityVo.getAttrValue());
            spuAttrValueEntity.setSort(0);
            return spuAttrValueEntity;
        }).collect(Collectors.toList());
        this.spuAttrValueService.saveBatch(spuAttrValueEntities);
    }

    private void saveSpuDesc(SpuEntityVo spuVo, Long spuId) {
        List<String> spuImages = spuVo.getSpuImages();
        String spuImagesStr = StringUtils.join(spuImages, ",");
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        spuDescEntity.setSpuId(spuId);
        spuDescEntity.setDecript(spuImagesStr);
        this.spuDescMapper.insert(spuDescEntity);
    }

    private Long saveSpu(SpuEntityVo spuVo) {
        spuVo.setPublishStatus(1);
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        this.save(spuVo);
        return spuVo.getId();
    }
}