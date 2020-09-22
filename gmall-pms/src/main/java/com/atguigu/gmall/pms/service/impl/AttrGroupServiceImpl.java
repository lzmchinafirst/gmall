package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryAttrGroupByCid(Long cid) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id",cid);
        List<AttrGroupEntity> list = this.list(wrapper);
        return list;
    }

    @Override
    public List<AttrGroupEntity> queryAttrGroupAndAttrByCid(Long catId) {
        QueryWrapper<AttrGroupEntity> groupWrapper = new QueryWrapper<>();
        groupWrapper.eq("category_id",catId);
        List<AttrGroupEntity> groupList = this.list(groupWrapper);
        if(CollectionUtils.isEmpty(groupList)) {
            return null;
        }
        groupList.forEach(a -> {
            QueryWrapper<AttrEntity> attrWrapper = new QueryWrapper<>();
            attrWrapper.eq("group_id",a.getId()).eq("type",1);
            List<AttrEntity> attrList = this.attrMapper.selectList(attrWrapper);
            a.setAttrEntities(attrList);
        });
        return groupList;
    }

}