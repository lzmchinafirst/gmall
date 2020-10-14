package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySkuAttrByCategoryIdAndSkuId(Long skuId, Long categoryId) {
        List<AttrEntity> attrEntityList = this.attrMapper.
                selectList(new QueryWrapper<AttrEntity>().
                        eq("category_id", categoryId).eq("search_type", 1));

        if (CollectionUtils.isEmpty(attrEntityList)) {
            return null;
        }
        List<Long> collect = attrEntityList.stream().map(AttrEntity::getId).collect(Collectors.toList());
        return this.list(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId).in("attr_id", collect));
    }

    @Override
    public List<SaleAttrValueVo> queryAllSaleElementsBySpuId(Long spuId) {
        List<AttrValueVo> attrValueVos = this.skuAttrValueMapper.queryAllSaleElementsBySpuId(spuId);
        List<SaleAttrValueVo> saleAttrValueVoList = new ArrayList<>();
        Map<Long, List<AttrValueVo>> collect = attrValueVos.stream().collect(Collectors.groupingBy(AttrValueVo::getAttrId));
        collect.forEach((key, value) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(key);
            saleAttrValueVo.setAttrName(value.get(0).getAttrName());
            Set<String> attrValues = value.stream().map(AttrValueVo::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(attrValues);
            saleAttrValueVoList.add(saleAttrValueVo);
        });
        return saleAttrValueVoList;
    }

    @Override
    public String getSaleAndSkuRelationshap(Long spuId) {
//        List<Map<Object, String>> relationshapList = this.skuAttrValueMapper.getSaleAndSkuRelationshap(spuId);
//        Map<Long, String> collect = relationshapList.stream().collect(Collectors.toMap(sku -> Long.valueOf(sku.get("sku_id")), sku -> sku.get("attr_values")));
//        return JSON.toJSONString(collect);

        List<Map<String, Object>> skus = this.skuAttrValueMapper.getSaleAndSkuRelationshap(spuId);
        // 转换成：{'暗夜黑,12G,512G': 3, '白天白,12G,512G': 4}
        Map<String, Long> map = skus.stream().collect(Collectors.toMap(sku -> sku.get("attr_values").toString(), sku -> (Long) sku.get("sku_id")));
        return JSON.toJSONString(map);

    }

    @Override
    public List<SkuAttrValueEntity> queryAllSalesBySkuId(Long skuId) {
        List<SkuAttrValueEntity> list = this.skuAttrValueMapper.selectList(new QueryWrapper<SkuAttrValueEntity>().eq("sku_id", skuId));
        return list;
    }

}