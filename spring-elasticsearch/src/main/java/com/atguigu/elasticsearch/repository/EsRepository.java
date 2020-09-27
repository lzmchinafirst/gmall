package com.atguigu.elasticsearch.repository;

import com.atguigu.elasticsearch.bean.Address;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface EsRepository extends ElasticsearchRepository<Address,Long> {

    List<Address> findByName(String name);
}
