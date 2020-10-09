package com.atguigu.rabbitmq.consumer.listener;

import ch.qos.logback.classic.turbo.TurboFilter;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ConsumerListener {

//    @RabbitListener(bindings = @QueueBinding(
//            value = @Queue(value = "sping-test-queue",durable = "true"),
//            exchange = @Exchange(value = "spring-test-exchange",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
//            key = {"a.b"}
//    ))
//    @RabbitListener(queues = {"spring-test-queue2"})
//    public void listener(String msg, Channel channel, Message message) throws IOException {
//        try {
//            System.out.println("work" + msg);
//            int a = 10 / 0;
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
//        } catch (Exception e) {
//            if(message.getMessageProperties().getRedelivered()){
//                //记录日志和信息，如果这里面绑定了死信队列那么如果requeue为false则进入素心队列
//                channel.basicReject(message.getMessageProperties().getDeliveryTag(),false);
//            } else {
//                channel.basicNack(message.getMessageProperties().getDeliveryTag(),false, true);
//            }
//        }
//    }

    @RabbitListener(queues = {"spring-dead-queue"})
    public void listenerDead(String msg,Channel channel, Message message) throws IOException {
        System.out.println("dead" + msg);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
