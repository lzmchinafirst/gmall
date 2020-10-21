package com.atguigu.gmall.cart.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.cart.pojo.Cart;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class OrderListener {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartMapper cartMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart-delete-queue",durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = "cart.delete"
    ))
    public void deleteCart(Map<String, Object> map, Message message, Channel channel) throws IOException {
        String userId = map.get("userId").toString();
        Object oldSkuIds = map.get("skuIds");
        List<Long> skuIds = JSON.parseArray(oldSkuIds.toString(), Long.class);
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(userId);
        skuIds.forEach(skuId -> {
            //异步删除redis中的购物车数据以及mysql数据库中的购物车数据
            hashOps.delete(skuId.toString());
            this.cartMapper.delete(new QueryWrapper<Cart>().eq("sku_id",skuId));
        });
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
