package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("search")
@Api(description = "根据上传的条件进行检索")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
    @ApiOperation("根据上传的条件进行检索")
    public String search(SearchParamVo searchParamVo, Model model){
        SearchResponseVo search = this.searchService.search(searchParamVo);
//        System.out.println("brand" + search.getBrands());
//        System.out.println("category" + search.getCategories());
//        System.out.println("filters" + search.getFilters());
        model.addAttribute("response", search);
        model.addAttribute("searchParam", searchParamVo);
        return "search";
    }
}
