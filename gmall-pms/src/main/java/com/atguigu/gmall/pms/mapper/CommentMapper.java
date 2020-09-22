package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CommentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价
 * 
 * @author lzm
 * @email lzm@atguigu.com
 * @date 2020-09-21 18:39:37
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
	
}
