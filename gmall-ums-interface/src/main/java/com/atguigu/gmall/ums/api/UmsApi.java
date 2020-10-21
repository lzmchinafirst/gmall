package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserAddressEntity;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface UmsApi {

    //根据用户的id查询用户的信息（包含积分信息）
    @GetMapping("ums/user/{id}")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id);

    //根据当前用户的id查询收货地址
    @GetMapping("ums/useraddress/user/{userId}")
    public ResponseVo<List<UserAddressEntity>> queryUserAddressByUserId(@PathVariable("userId")Long userId);

    //校验用户数据的唯一性
    @GetMapping("ums/user/check/{data}/{type]")
    public ResponseVo<Boolean> userDataCheck(@PathVariable("data") String data, @PathVariable("type") Integer type);

    //注册用户
    @PostMapping("ums/user/register")
    public ResponseVo userRegister(@RequestBody UserEntity userEntity, @RequestParam("code")String code);

    //发送验证码
    @PostMapping("ums/user/code")
    public ResponseVo sendCode(@RequestParam("phone") String phone);

    //用户登录
    @GetMapping("ums/user/query")
    public ResponseVo<String> queryUser(@RequestParam("loginName")String loginName,
                                        @RequestParam("password")String password);
}
