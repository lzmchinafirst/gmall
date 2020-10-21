package com.atguigu.gmall.ums.controller;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 用户表
 *
 * @author lzm
 * @email lzm@atguigu.com
 * @date 2020-09-21 18:48:36
 */
@Api(tags = "用户表 管理")
@RestController
@RequestMapping("ums/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 实现用户数据的校验，主要包括对：手机号、用户名的唯一性校验。
     * GET /ums/user/check/{data}/{type}
     */
    @ApiOperation("用户数据的校验")
    @GetMapping("check/{data}/{type]")
    public ResponseVo<Boolean> userDataCheck(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        Boolean check = this.userService.userDataCheck(data, type);
        return ResponseVo.ok(check);
    }

    /**
     * POST /ums/user/code
     * 根据用户输入的手机号，生成随机验证码，长度为6位，纯数字。并且调用短信服务，发送验证码到用户手机。
     * phone:用户的手机号码
     */
    @ApiOperation("发送验证码")
    @PostMapping("code")
    public ResponseVo sendCode(@RequestParam("phone") String phone) {
        this.userService.sendCode(phone);
        return ResponseVo.ok();
    }

    /**
     * POST /ums/user/register
     * 实现用户注册功能，需要对用户密码进行加密存储，使用MD5加密，
     * 加密过程中使用随机码作为salt加盐。另外还需要对用户输入的短信验证码进行校验。
     * username用户名，格式为4~30位字母、数字、下划线是String无
     * password用户密码，格式为4~30位字母、数字、下划线是String无
     * phone手机号码是String无
     * email邮箱是String无
     * code短信验证码是String无
     */
    @ApiOperation("用户注册")
    @PostMapping("register")
    public ResponseVo userRegister(@RequestBody UserEntity userEntity,@RequestParam("code")String code) {
        this.userService.userRegister(userEntity,code);
        return ResponseVo.ok();
    }

    /**
     * GET /ums/user/query
     * loginName用户名/手机号/邮箱，格式为4~30位字母、数字、下划线是String无
     * password用户密码，格式为4~30位字母、数字、下划线是String无
     */
    @ApiOperation("查询用户")
    @GetMapping("query")
    public ResponseVo<String> queryUser(@RequestParam("loginName")String loginName,
                                        @RequestParam("password")String password) {
        UserEntity user = this.userService.queryUser(loginName,password);
        return ResponseVo.ok(JSON.toJSONString(user));
    }


    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = userService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id) {
        UserEntity user = userService.getById(id);

        return ResponseVo.ok(user);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserEntity user) {
        userService.save(user);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserEntity user) {
        userService.updateById(user);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        userService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
