package com.atguigu.gmall.ums.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

public interface UmsApi {

    //校验用户数据的唯一性
    @GetMapping("ums/user/check/{data}/{type]")
    public ResponseVo<Boolean> userDataCheck(@PathVariable("data") String data, @PathVariable("type") Integer type);
    //注册用户
    @PostMapping("ums/user/register")
    public ResponseVo userRegister(@RequestBody UserEntity userEntity, @RequestParam("code")String code);

    //发送验证码
    @PostMapping("code")
    public ResponseVo sendCode(@RequestParam("phone") String phone);
}
