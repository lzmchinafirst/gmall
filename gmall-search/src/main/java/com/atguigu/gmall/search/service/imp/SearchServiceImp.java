package com.atguigu.gmall.search.service.imp;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrValueVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImp implements SearchService {


    @Autowired
    private RestHighLevelClient restClient;


    private static final ObjectMapper MAPPER = new ObjectMapper();


    /**
     * 返回对应的SearchReturnVo对象
     * @param searchParamVo
     */
    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, getSearchSourceBuilder(searchParamVo));
            SearchResponse search = this.restClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchResponseVo searchReturn = getSearchReturn(search);
            //设置当前页
            searchReturn.setPageNum(searchParamVo.getPageNum());
            //设置每页的记录数
            searchReturn.setPageSize(searchParamVo.getPageSize());
            return searchReturn;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析生成的SearchResponse对象，返回对应的SearchReturnVo对象
     * @param searchResponse
     * @return
     */
    public SearchResponseVo getSearchReturn(SearchResponse searchResponse) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
        Long total = hits.getTotalHits();
        //设置总的记录数
        searchResponseVo.setTotal(total);
        SearchHit[] hitsHits = hits.getHits();
        //得到对应的结果集
        List<Goods> goodsList = Arrays.stream(hitsHits).map(hit -> {
            try {
                String sourceAsString = hit.getSourceAsString();
                Goods goods = MAPPER.readValue(sourceAsString, Goods.class);
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                String title = highlightFields.get("title").toString();
                //将结果集的标题设置为高亮显示
                goods.setTitle(title);
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            }
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);
        //分页数据解析完成，下面开始解析聚合数据
        Map<String, Aggregation> allAggs = searchResponse.getAggregations().asMap();
        //1.封装品牌
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) allAggs.get("brandIdAgg");
        List<? extends Terms.Bucket> brandIdBuckets = brandIdAgg.getBuckets();
        List<BrandEntity> brands = brandIdBuckets.stream().map(bucket -> {
            BrandEntity brandEntity = new BrandEntity();
            Long brandId = (Long) bucket.getKey();
            brandEntity.setId(brandId);
            Map<String, Aggregation> brandIdSubAggs = bucket.getAggregations().asMap();
            ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandIdSubAggs.get("brandNameAgg");
            List<? extends Terms.Bucket> brandNameBuckets = brandNameAgg.getBuckets();
            String brandName = (String) brandNameBuckets.get(0).getKey();
            ParsedStringTerms logoAgg = (ParsedStringTerms) brandIdSubAggs.get("logoAgg");
            List<? extends Terms.Bucket> logoBuckets = logoAgg.getBuckets();
            String brandLogo = (String) logoBuckets.get(0).getKey();
            brandEntity.setName(brandName);
            brandEntity.setLogo(brandLogo);
            return brandEntity;
        }).collect(Collectors.toList());
        searchResponseVo.setBrands(brands);
        //2.封装分类
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) allAggs.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdBuckets = categoryIdAgg.getBuckets();
        List<CategoryEntity> categorys = categoryIdBuckets.stream().map(categoryBucket -> {
            CategoryEntity categoryEntity = new CategoryEntity();
            Long categoryId = (Long) categoryBucket.getKey();
            Map<String, Aggregation> categoryIdSubAggs = categoryBucket.getAggregations().asMap();
            ParsedStringTerms categoryNameAgg = (ParsedStringTerms) categoryIdSubAggs.get("categoryNameAgg");
            List<? extends Terms.Bucket> categoryNameAggBuckets = categoryNameAgg.getBuckets();
            String categoryName = (String) categoryNameAggBuckets.get(0).getKey();
            categoryEntity.setId(categoryId);
            categoryEntity.setName(categoryName);
            return categoryEntity;
        }).collect(Collectors.toList());
        searchResponseVo.setCategories(categorys);
        //3.封装规格参数
        ParsedNested attrAgg = (ParsedNested) allAggs.get("attrAgg");
        Map<String, Aggregation> attrAggSubAggs = attrAgg.getAggregations().asMap();
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrAggSubAggs.get("attrIdAgg");
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        List<SearchResponseAttrValueVo> searchResponseAttrValueVoList = buckets.stream().map(bucket -> {
            SearchResponseAttrValueVo vo = new SearchResponseAttrValueVo();
            Long key = (Long) bucket.getKey();
            vo.setAttrId(key);
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            List<? extends Terms.Bucket> attrNameAggBuckets = attrNameAgg.getBuckets();
            String attrName = (String) attrNameAggBuckets.get(0).getKey();
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
            List<String> attrValueList = attrValueAggBuckets.stream().map(attrValueAggBucket -> {
                String attrValue = (String) attrValueAggBucket.getKey();
                return attrValue;
            }).collect(Collectors.toList());
            vo.setAttrName(attrName);
            vo.setAttrValues(attrValueList);
            return vo;
        }).collect(Collectors.toList());
        searchResponseVo.setFilters(searchResponseAttrValueVoList);
        return searchResponseVo;
    }

    /**
     * 根据上传的结果返回对应的SearchSourceBuilder对象
     * @param searchParamVo
     * @return
     */
    public SearchSourceBuilder getSearchSourceBuilder(SearchParamVo searchParamVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String keyword = searchParamVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            //TODO 这里可以插入广告
            return searchSourceBuilder;
        }
        //1.匹配查询（搜索框上传）
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));

        //2.过滤查询
        //2.1品牌过滤
        List<Long> brandIdList = searchParamVo.getBrandId();
        if(CollectionUtils.isNotEmpty(brandIdList)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brandIdList));
        }

        //2.2分类过滤
        List<Long> cid3 = searchParamVo.getCid3();
        if(CollectionUtils.isNotEmpty(cid3)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId",cid3));
        }

        //2.3价格过滤
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if(priceFrom != null || priceTo != null){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            if(priceFrom != null) {
                rangeQueryBuilder.gte(priceFrom);
            }
            if(priceTo != null) {
                rangeQueryBuilder.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }
        //2.4是否有货
        Boolean store = searchParamVo.getStore();
        if(store != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("store",store));
        }
        //2.5检索条件过滤
        List<String> props = searchParamVo.getProps();
        if(CollectionUtils.isNotEmpty(props)) {
            props.forEach(prop ->{
                BoolQueryBuilder bool = QueryBuilders.boolQuery();
                String[] attrs = StringUtils.split(prop, ":");
                if(attrs != null && attrs.length == 2) {
                    bool.must(QueryBuilders.termQuery("searchAttrs.attrId",attrs[0]));
                    String[] attrValues = StringUtils.split(attrs[1], "-");
                    bool.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                }
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",bool, ScoreMode.None));
            });
        }
        searchSourceBuilder.query(boolQueryBuilder);
        //3.排序 1-价格升序 2-价格降序 3-新品降序 4-销量降序
        Integer sort = searchParamVo.getSort();
        if(sort == 1) {
           searchSourceBuilder.sort("price", SortOrder.ASC);
        }
        if(sort == 2) {
            searchSourceBuilder.sort("price", SortOrder.DESC);
        }
        if(sort == 1) {
            searchSourceBuilder.sort("createTime", SortOrder.DESC);
        }
        if(sort == 1) {
            searchSourceBuilder.sort("sales", SortOrder.DESC);
        }
        //4.分页
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        //5.高亮
        searchSourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<em>").postTags("</em>"));
        //6.聚合
        //6.1聚合品牌
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId").
                subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")).
                subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        //6.2聚合分类
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId").
                subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //6.3聚合检索条件
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","searchAttrs").
                subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId").
                        subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName")).
                        subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));
        //7.结果集过滤
        searchSourceBuilder.fetchSource(new String[]{"skuId", "title", "subTitle", "price", "defaultImage"},null);
        System.out.println("searchSourceBuilder" + searchSourceBuilder);
        return searchSourceBuilder;
    }
}
