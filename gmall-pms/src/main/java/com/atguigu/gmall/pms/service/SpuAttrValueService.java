package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author lzm
 * @email lzm@atguigu.com
 * @date 2020-09-21 18:39:37
 */
public interface SpuAttrValueService extends IService<SpuAttrValueEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    List<SpuAttrValueEntity> querySpuAttrValueBySpuIdAndCategoryId(Long spuId, Long categoryId);

}

