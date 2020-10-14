package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.CategoryEntityExtend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.CategoryMapper;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<CategoryEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageResultVo(page);
    }


    @Override
    public List<CategoryEntity> queryCategoryByParentId(Long parentId) {
        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
        if(parentId != -1) {
            wrapper.eq("parent_id",parentId);
        }
        List<CategoryEntity> list = this.list(wrapper);
        return list;
    }

    @Override
    public List<CategoryEntity> queryCategories(Long cid) {
        List<CategoryEntity> subs = this.categoryMapper.queryCategories(cid);
        return subs;
    }

    @Override
    public List<CategoryEntity> queryAllCategoriesByLv3Id(Long cid3) {
        List<CategoryEntity> categoryEntityList = new ArrayList<>();
        CategoryEntity categoryEntityLv3 = this.categoryMapper.selectById(cid3);
        if(categoryEntityLv3 != null) {
            categoryEntityList.add(categoryEntityLv3);
            Long cid2 = categoryEntityLv3.getParentId();
            CategoryEntity categoryEntityLv2 = this.categoryMapper.selectById(cid2);
            categoryEntityList.add(categoryEntityLv2);
            Long cid = categoryEntityLv2.getParentId();
            CategoryEntity categoryEntityLv1 = this.categoryMapper.selectById(cid);
            categoryEntityList.add(categoryEntityLv1);
        }
        return categoryEntityList;
    }

}