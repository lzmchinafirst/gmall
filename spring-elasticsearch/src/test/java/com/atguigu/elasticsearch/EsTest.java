package com.atguigu.elasticsearch;


import com.atguigu.elasticsearch.bean.Address;
import com.atguigu.elasticsearch.repository.EsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.security.user.User;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.engine.Engine;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.ParsedAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.fetch.subphase.highlight.SourceSimpleFragmentsBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;

import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest
public class EsTest {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private EsRepository esRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    void esTest01() {
        this.restTemplate.createIndex(Address.class);
        this.restTemplate.putMapping(Address.class);
    }

    @Test
    void esTest02() {
        esRepository.save(new Address(1L, "李晨", 10, "123456"));
    }

    @Test
    void esTest03() {
        Optional<Address> byId = esRepository.findById(1L);
        System.out.println(byId);
        esRepository.deleteById(1L);
    }

    @Test
    void esTest04() {
        Address address = esRepository.findById(1l).get();
        System.out.println(address);
        esRepository.findAll().forEach(a -> {
            System.out.println(a);
        });
    }

    @Test
    void esTest05() {
        List<Address> address = esRepository.findByName("李晨");
        address.forEach(a -> {
            System.out.println(a);
        });
    }

    @Test
    void esTest06() {
        List<Address> addresses = new ArrayList<>();
        addresses.add(new Address(1l, "柳冰冰", 18, "123456"));
        addresses.add(new Address(2l, "范冰冰", 19, "123456"));
        addresses.add(new Address(3l, "李冰冰", 20, "123456"));
        addresses.add(new Address(4l, "锋哥", 21, "123456"));
        addresses.add(new Address(5l, "小冰冰", 22, "123456"));
        addresses.add(new Address(6l, "韩冰冰", 23, "123456"));
        this.esRepository.saveAll(addresses);
    }

    @Test
    void esTest07() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("name", "冰冰"));
        queryBuilder.withQuery(QueryBuilders.rangeQuery("age").lte(20).gte(19));
        Page<Address> search = this.esRepository.search(queryBuilder.build());
        int totalPages = search.getTotalPages();
        long totalElements = search.getTotalElements();
        System.out.println("总页数" + totalPages);
        System.out.println("总记录数" + totalElements);
        List<Address> content = search.getContent();
        content.forEach(a -> {
            System.out.println(a);
        });
    }

    @Test
    void esTest08() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("name", "冰冰"));
        //排序
        queryBuilder.withSort(new FieldSortBuilder("age").order(SortOrder.DESC));
        //分页（第二页，每页的数据为两条）
        queryBuilder.withPageable(PageRequest.of(1, 2));
        //高亮
        queryBuilder.withHighlightBuilder(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));
        //添加aggs
        queryBuilder.addAggregation(AggregationBuilders.terms("passwordagg").field("password"));
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"name", "age"}, null));
        AggregatedPage<Address> search = (AggregatedPage<Address>) this.esRepository.search(queryBuilder.build());
        System.out.println("search.getTotalElements() = " + search.getTotalElements());
        System.out.println("search.getTotalPages() = " + search.getTotalPages());
        System.out.println(search.getContent());
        ParsedStringTerms passwordagg = (ParsedStringTerms) search.getAggregation("passwordagg");
        passwordagg.getBuckets().forEach(bucket -> {
            System.out.println(bucket.getKeyAsString());
            System.out.println(bucket.getDocCount());
        });
    }

    private final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void esTest09() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //匹配查询
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "冰冰"));
        //顺序排序
        searchSourceBuilder.sort("age", SortOrder.DESC);
        //分页查询
        searchSourceBuilder.from(2);
        searchSourceBuilder.size(2);
        //资源过滤
        searchSourceBuilder.fetchSource(new String[]{"id", "name", "age"}, null);
        //高亮显示
        searchSourceBuilder.highlighter(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));
        //添加aggregation
        searchSourceBuilder.aggregation(AggregationBuilders.terms("passwordAgg").field("password"));
        SearchRequest searchRequest = new SearchRequest(new String[]{"entertainment"},searchSourceBuilder);
        SearchResponse search = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(search);
        //得到对应的结果
        SearchHits hits = search.getHits();
        System.out.println("命中记录数" + hits.getTotalHits());
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hitsHit : hitsHits) {
            String sourceAsString = hitsHit.getSourceAsString();
            Address address = MAPPER.readValue(sourceAsString, Address.class);
            System.out.println("user" + address.toString());
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField name = highlightFields.get("name");
            Text[] fragments = name.getFragments();
            address.setName(fragments[0].toString());
            System.out.println("高亮user" + address);
        }
        Map<String, Aggregation> stringAggregationMap = search.getAggregations().asMap();
        ParsedStringTerms passwordAgg = (ParsedStringTerms) stringAggregationMap.get("passwordAgg");
        List<? extends Terms.Bucket> buckets = passwordAgg.getBuckets();
        buckets.forEach(a -> {
            System.out.println(a.getKeyAsString());
            System.out.println(a.getDocCount());
        });
    }
}
