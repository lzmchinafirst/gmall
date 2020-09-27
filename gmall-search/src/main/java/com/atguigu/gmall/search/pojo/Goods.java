package com.atguigu.gmall.search.pojo;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.Date;
import java.util.List;

@Data
@Document(indexName = "goods", type = "info",shards = 3, replicas = 2)
public class Goods{

    //搜索列表字段
    @Id
    private Long skuId;
    //标题
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    //副标题
    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;
    //默认图片
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;
    //价格
    @Field(type = FieldType.Double)
    private Double price;

    //排序和筛选字段
    //销量
    @Field(type = FieldType.Long)
    private Long sales = 0L;
    //新品
    @Field(type = FieldType.Date)
    private Date createTime;
    //是否有货
    @Field(type = FieldType.Boolean)
    private boolean store = false;

    //聚合字段
    //品牌id
    @Field(type = FieldType.Long)
    private Long brandId;
    //品牌名称
    @Field(type = FieldType.Keyword)
    private String brandName;
    //品牌logo
    @Field(type = FieldType.Keyword)
    private String logo;
    //分类id
    @Field(type = FieldType.Long)
    private Long categoryId;
    //分类名称
    @Field(type = FieldType.Keyword)
    private String categoryName;
    //查询属性
    @Field(type = FieldType.Nested)
    private List<SearchAttrValue> searchAttrs;
}