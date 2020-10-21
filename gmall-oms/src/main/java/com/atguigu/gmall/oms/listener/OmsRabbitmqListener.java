package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.common.exception.OrderException;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OmsRabbitmqListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "OMS-CLOSE-ORDER", durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = "ORDER.CLOSE"
    ))
    public void closeOrder(String orderToken, Message message, Channel channel) throws IOException {
        //查询数据库中是否有该条订单记录
        OrderEntity orderEntity = this.orderMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));
        if(orderEntity == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //如果有那么将订单的状态设置为4
        orderEntity.setStatus(4);
        //修改订单的状态
        int i = this.orderMapper.updateById(orderEntity);
        if(i != 1) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            throw new OrderException("订单修改异常！");
        }
        //向mq中发送消息，通知被锁定的库存进行解锁
        this.rabbitTemplate.convertAndSend("ORDER-EXCHANGE","SEND.DEAD",orderToken);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "OMS-DISABLE-QUEUE",durable = "true"),
            exchange = @Exchange(value = "ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = "ORDER.DISABLE"
    ))
    public void disableOrder(String orderToken, Message message, Channel channel) throws IOException {
        OrderEntity orderEntity = this.orderMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderToken));
        if(orderEntity == null) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            return;
        }
        //如果订单存在的话那么将其设置为无效订单
        orderEntity.setStatus(5);
        int i = this.orderMapper.updateById(orderEntity);
        if(i != 1) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            throw new OrderException("订单修改异常！");
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
