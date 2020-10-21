package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.mapper.WareSkuMapper;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class WmsRabbitListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WareSkuMapper wareSkuMapper;

    private static final String PRE_KEY =  "stock:lock:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "UNLOCK-STOCK-QUEUE",durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = "SEND.DEAD"
    ))
    public void unLockStockListener(String orderToken, Message message, Channel channel) throws IOException {
        String skuListJson = this.redisTemplate.opsForValue().get(PRE_KEY + orderToken);
        //如果查询到的json数据的值为空的话，那么直接return
        if(skuListJson == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        List<SkuLockVo> skuLockVoList = JSON.parseArray(skuListJson, SkuLockVo.class);
        if(CollectionUtils.isEmpty(skuLockVoList)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //如果不为空的话我们要将所有已锁的库存解锁
        skuLockVoList.forEach(skuLockVo -> {
            this.wareSkuMapper.lock(skuLockVo.getWareSkuId(),- (skuLockVo.getCount()));
        });
        //为了防止重复解锁我们要将redis中对应的数据删除
        this.redisTemplate.delete(PRE_KEY + orderToken);
        //手动提交
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
