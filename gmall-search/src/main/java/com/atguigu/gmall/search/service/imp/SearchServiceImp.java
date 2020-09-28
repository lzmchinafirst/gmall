package com.atguigu.gmall.search.service.imp;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SearchServiceImp implements SearchService {


    @Autowired
    private RestHighLevelClient restClient;

    @Override
    public void search(SearchParamVo searchParamVo) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, getSearchSourceBuilder(searchParamVo));
            SearchResponse search = this.restClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(search);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SearchSourceBuilder getSearchSourceBuilder(SearchParamVo searchParamVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String keyword = searchParamVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            //TODO 这里可以插入广告
            return searchSourceBuilder;
        }
        //1.匹配查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        searchSourceBuilder.query(boolQueryBuilder);
        boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));


        System.out.println(searchSourceBuilder);
        return searchSourceBuilder;
    }
}
