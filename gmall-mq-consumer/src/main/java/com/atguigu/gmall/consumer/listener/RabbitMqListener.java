package com.atguigu.gmall.consumer.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.CodeModel;
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
import java.util.concurrent.TimeUnit;

@Component
public class RabbitMqListener {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "codeQueue", durable = "true"),
            exchange = @Exchange(value = "UMS-CODE-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"USER.CODE"}))
    public void listener(String codeModel, Channel channel, Message message) throws IOException {
        try {
            CodeModel codeModel1 = JSON.parseObject(codeModel, CodeModel.class);
            String phone = codeModel1.getPhone();
            String code = codeModel1.getCode();
            System.out.println(codeModel.toString());
            this.redisTemplate.opsForValue().set("user:code:" + phone,code,1, TimeUnit.MINUTES);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            if (message.getMessageProperties().getRedelivered()) {
                //记录日志和信息，如果这里面绑定了死信队列那么如果requeue为false则进入素心队列
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }
}
