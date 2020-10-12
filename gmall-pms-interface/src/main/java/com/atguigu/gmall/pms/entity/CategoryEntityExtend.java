package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("pms_category")
public class CategoryEntityExtend extends CategoryEntity{

    @TableField(exist = false)
    private List<CategoryEntity> subs;
}
