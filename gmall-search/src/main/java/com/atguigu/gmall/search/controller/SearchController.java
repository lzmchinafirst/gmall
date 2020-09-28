package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.ws.Response;

@RestController
@RequestMapping("search")
@Api(description = "根据上传的条件进行检索")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    @ApiOperation("根据上传的条件进行检索")
    public ResponseVo<Object> search(SearchParamVo searchParamVo){
        this.searchService.search(searchParamVo);
        return ResponseVo.ok();
    }
}
