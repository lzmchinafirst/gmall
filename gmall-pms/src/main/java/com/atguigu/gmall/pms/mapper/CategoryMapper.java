package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.CategoryEntityExtend;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import javax.ws.rs.Path;
import java.util.List;

/**
 * 商品三级分类
 * 
 * @author lzm
 * @email lzm@atguigu.com
 * @date 2020-09-21 18:39:37
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {

    List<CategoryEntity> queryCategories(@Param("cid") Long cid);
}
