package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuEntityVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author lzm
 * @email lzm@atguigu.com
 * @date 2020-09-21 18:39:37
 */
public interface SpuService extends IService<SpuEntity> {

    PageResultVo queryPage(PageParamVo paramVo);

    PageResultVo querySpuByCategoryId(Long cid, PageParamVo pageParamVo);

    void saveSpuEntityVo(SpuEntityVo spu);
}

