package com.atguigu.gmall.pms.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.service.SkuService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * sku信息
 *
 * @author lzm
 * @email lzm@atguigu.com
 * @date 2020-09-21 18:39:36
 */
@Api(tags = "sku信息 管理")
@RestController
@RequestMapping("pms/sku")
public class SkuController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SkuService skuService;

    /**
     * 查询spu的所有sku信息
     */
    @GetMapping("spu/{spuId}")
    @ApiOperation("根据spu的id查询对应的所有的sku的信息")
    public ResponseVo<List<SkuEntity>> queryAllSkuInItSpu(@PathVariable("spuId")Long spuId) {
        QueryWrapper<SkuEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id",spuId);
        List<SkuEntity> list = this.skuService.list(wrapper);
        return ResponseVo.ok(list);
    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> querySkuByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = skuService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id){
		SkuEntity sku = skuService.getById(id);

        return ResponseVo.ok(sku);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody SkuEntity sku){
		skuService.save(sku);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody SkuEntity sku){
		skuService.updateById(sku);
        this.rabbitTemplate.convertAndSend("GMALL.PMS.EXCHANGE","pms.price", JSON.toJSONString(sku));
        ArrayList arrayList = new ArrayList();
        LinkedList linkedList = new LinkedList();
        linkedList.add("你好");
        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		skuService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
