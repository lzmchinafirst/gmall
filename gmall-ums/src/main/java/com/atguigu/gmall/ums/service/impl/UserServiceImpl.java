package com.atguigu.gmall.ums.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.CodeModel;
import com.atguigu.gmall.common.bean.DivException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;

import java.util.Date;
import java.util.UUID;


@Service("userService")
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    //要校验的数据类型：1，用户名；2，手机；3，邮箱
    @Override
    public Boolean userDataCheck(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone",data);
                break;
            case 3:
                wrapper.eq("email",data);
                break;
            default:
                return null;
        }
        int count = this.count(wrapper);
        return count == 0;
    }

    @Override
    public void sendCode(String phone) {
//        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "<accessKeyId>", "<accessSecret>");
//        IAcsClient client = new DefaultAcsClient(profile);
//
//        CommonRequest request = new CommonRequest();
//        request.setSysMethod(MethodType.POST);
//        request.setSysDomain("dysmsapi.aliyuncs.com");
//        request.setSysVersion("2017-05-25");
//        request.setSysAction("SendSms");
//        request.putQueryParameter("RegionId", "cn-hangzhou");
//        request.putQueryParameter("PhoneNumbers", phone);
//        request.putQueryParameter("SignName", "我的谷粒学院的签名");
//        request.putQueryParameter("TemplateCode", "SMS_201455302");
//        try {
//            CommonResponse response = client.getCommonResponse(request);
//            System.out.println(response.getData());
//        } catch (ServerException e) {
//            e.printStackTrace();
//        } catch (ClientException e) {
//            e.printStackTrace();
//        }
        String code = UUID.randomUUID().toString().substring(0, 6);
        new Thread(() -> {
            CodeModel codeModel = new CodeModel();
            codeModel.setPhone(phone);
            codeModel.setCode(code);
            this.rabbitTemplate.convertAndSend("UMS-CODE-EXCHANGE","USER.CODE", JSON.toJSONString(codeModel));
        }).start();
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
    @Override
    public void userRegister(UserEntity userEntity, String code) {
        //TODO 到redis中查询对应的验证码并进行校验
        String redisCacheCode = this.redisTemplate.opsForValue().get("user:code:" + userEntity.getPhone());
        if(redisCacheCode == null) {
            log.error("==================WARNING:your cache is not exist====================");
            return;
        }
        String password = userEntity.getPassword();
        String salt = UUID.randomUUID().toString().substring(0,6);
        userEntity.setSalt(salt);
        userEntity.setPassword(DigestUtils.md5Hex(password + salt));
        userEntity.setLevelId(1l);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());
        userEntity.setSourceType(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        this.save(userEntity);

        //TODO 删除redis中对应的验证码数据
        this.redisTemplate.delete("user:code:" + userEntity.getPhone());
    }

    /**
     * GET /ums/user/query
     * loginName用户名/手机号/邮箱，格式为4~30位字母、数字、下划线是String无
     * password用户密码，格式为4~30位字母、数字、下划线是String无
     */
    @Override
    public UserEntity queryUser(String loginName, String password) {
        QueryWrapper<UserEntity> userWrapper = new QueryWrapper<>();
        userWrapper.eq("username",loginName).
                    or().eq("phone",loginName).
                    or().eq("email",loginName);
        UserEntity userEntity = this.getOne(userWrapper);
        if(userEntity == null) {
            return null;
        }
        String salt = userEntity.getSalt();
        password = DigestUtils.md5Hex(password + salt);
        if(!StringUtils.equals(password,userEntity.getPassword())) {
            return null;
        }
        return userEntity;
    }
}