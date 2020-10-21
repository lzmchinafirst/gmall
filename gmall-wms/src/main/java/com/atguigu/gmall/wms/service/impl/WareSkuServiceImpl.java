package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.DivException;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuMapper, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuMapper wareSkuMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private static final String PRE_KEY =  "stock:lock:";

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<WareSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLock(List<SkuLockVo> skuLockVos) {
        if(CollectionUtils.isEmpty(skuLockVos)) {
            return null;
        }
        skuLockVos.forEach((skuLockVo) -> {
            this.checkLock(skuLockVo);
        });
        //锁定成功的数据
        List<SkuLockVo> success = skuLockVos.stream().filter(skuLockVo -> skuLockVo.getLock()).collect(Collectors.toList());
        //锁定失败的数据
        List<SkuLockVo> fall = skuLockVos.stream().filter(skuLockVo -> !skuLockVo.getLock()).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(fall)){
            //但凡有一个失败，全部都要解锁
            success.forEach((a) -> {
                a.setLock(false);
                RLock fairLock = redissonClient.getFairLock("lock" + a.getSkuId());
                fairLock.lock();
                this.wareSkuMapper.lock(a.getWareSkuId(),-(a.getCount()));
                fairLock.unlock();
            });
            return skuLockVos;
        }
        //将库存信息的锁定信息保存到Redis中
        this.redisTemplate.opsForValue().set(PRE_KEY + skuLockVos.get(0).getOrderToken(), JSON.toJSONString(skuLockVos));

        //随后发送消息给延时队列，让其120秒钟以后自动的解锁
        this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","SEND.TTL",skuLockVos.get(0).getOrderToken());
        return null;
    }

    private void checkLock(SkuLockVo skuLockVo) {
        RLock fairLock = redissonClient.getFairLock("lock:" + skuLockVo.getSkuId());
        fairLock.lock();
        List<WareSkuEntity> wareSkuEntityList = this.wareSkuMapper.checkStore(skuLockVo.getSkuId(),skuLockVo.getCount());
        //如果没有符合要求的仓库我们将将lock属性设置为false
        if(CollectionUtils.isEmpty(wareSkuEntityList)) {
            skuLockVo.setLock(false);
            fairLock.unlock();
            return;
        }
        //这里对库存进行锁定，由于条件有限我们采取了锁定第一个库的做法
        WareSkuEntity wareSkuEntity = wareSkuEntityList.get(0);
        if(this.wareSkuMapper.lock(wareSkuEntity.getId(),skuLockVo.getCount()) == 1) {
            //设置锁定状态为true
            skuLockVo.setLock(true);
            //设置锁定的仓库id（其实是wms_ware_sku这张表的id）
            skuLockVo.setWareSkuId(wareSkuEntity.getId());
        } else {
            skuLockVo.setLock(false);
        }
        fairLock.unlock();
    }

}