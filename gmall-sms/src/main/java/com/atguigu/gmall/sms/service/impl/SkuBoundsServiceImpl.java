package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.mapper.SkuFullReductionMapper;
import com.atguigu.gmall.sms.mapper.SkuLadderMapper;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.apache.ibatis.executor.ResultExtractor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {

    @Autowired
    private SkuFullReductionMapper skuFullReductionMapper;

    @Autowired
    private SkuLadderMapper skuLadderMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * 向sms_sku_bond表中添加数据
     * 向sms_sku_full_reduction表中添加数据
     * 向sms_sku_ladder表中添加数据
     * @param skuSaleVo
     */
    @Override
    @Transactional
    public void skuSave(SkuSaleVo skuSaleVo) {
        if(skuSaleVo != null) {
            //第一步：向sms_sku_bond表中添加数据
            saveSkuBounds(skuSaleVo);
            //第二步：向sms_sku_full_reduction表中添加数据
            saveSkuFullReduction(skuSaleVo);
            //第三步：向sms_ladder表中添加数据
            saveSkuLadder(skuSaleVo);
        }
    }

    @Override
    public List<ItemSaleVo> queryAllSaleStrategy(Long skuid) {
        List<ItemSaleVo> resultList = new ArrayList<>();
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuid));
        if(skuBoundsEntity != null) {
            BigDecimal buyBounds = skuBoundsEntity.getBuyBounds();
            BigDecimal growBounds = skuBoundsEntity.getGrowBounds();
            ItemSaleVo bound = new ItemSaleVo();
            bound.setType("积分");
            bound.setDesc("购买此商品赠送" + growBounds + "成长积分，" + buyBounds + "购物积分");
            resultList.add(bound);
        }
        SkuLadderEntity skuLadderEntity = this.skuLadderMapper.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuid));
        if(skuLadderEntity != null) {
            ItemSaleVo ladder = new ItemSaleVo();
            ladder.setType("打折");
            ladder.setDesc("购买本商品满" + skuLadderEntity.getFullCount() + "件，打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            resultList.add(ladder);
        }
        SkuFullReductionEntity skuFullReductionEntity = this.skuFullReductionMapper.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuid));
        if(skuFullReductionEntity != null) {
            ItemSaleVo fullReduction = new ItemSaleVo();
            fullReduction.setType("满减");
            fullReduction.setDesc("购买本商品，满" + skuFullReductionEntity.getFullPrice() + "减" + skuFullReductionEntity.getReducePrice() + "元");
            resultList.add(fullReduction);
        }
        return resultList;
    }

    private void saveSkuLadder(SkuSaleVo skuSaleVo) {
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuSaleVo,skuLadderEntity);
        skuLadderEntity.setAddOther(skuSaleVo.getLadderAddOther());
        skuLadderMapper.insert(skuLadderEntity);
    }

    private void saveSkuFullReduction(SkuSaleVo skuSaleVo) {
        SkuFullReductionEntity skuFullReductionEntity  = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuSaleVo,skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuSaleVo.getFullAddOther());
        skuFullReductionMapper.insert(skuFullReductionEntity);
    }

    private void saveSkuBounds(SkuSaleVo skuSaleVo) {
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        BeanUtils.copyProperties(skuSaleVo,skuBoundsEntity);
        List<Integer> work = skuSaleVo.getWork();
        if(CollectionUtils.isNotEmpty(work)) {
            Integer workInteger  = work.get(0) * 8 + work.get(1) * 4 + work.get(2) * 2 + work.get(3);
            skuBoundsEntity.setWork(workInteger);
        }
        this.save(skuBoundsEntity);
    }

}