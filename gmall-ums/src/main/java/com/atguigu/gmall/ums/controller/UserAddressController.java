package com.atguigu.gmall.ums.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.service.UserAddressService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 收货地址表
 *
 * @author lzm
 * @email lzm@atguigu.com
 * @date 2020-09-21 18:48:36
 */
@Api(tags = "收货地址表 管理")
@RestController
@RequestMapping("ums/useraddress")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;


    //根据当前用户的id查询收货地址
    @GetMapping("user/{userId}")
    public ResponseVo<List<UserAddressEntity>> queryUserAddressByUserId(@PathVariable("userId")Long userId) {
        List<UserAddressEntity> userAddressEntityList = this.userAddressService.queryUserAddress(userId);
        return ResponseVo.ok(userAddressEntityList);
    }


    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserAddressByPage(PageParamVo paramVo){
        PageResultVo pageResultVo = userAddressService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserAddressEntity> queryUserAddressById(@PathVariable("id") Long id){
		UserAddressEntity userAddress = userAddressService.getById(id);

        return ResponseVo.ok(userAddress);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserAddressEntity userAddress){
		userAddressService.save(userAddress);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserAddressEntity userAddress){
		userAddressService.updateById(userAddress);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		userAddressService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
