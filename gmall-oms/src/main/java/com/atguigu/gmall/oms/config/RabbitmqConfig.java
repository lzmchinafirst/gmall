package com.atguigu.gmall.oms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.rsocket.context.LocalRSocketServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class RabbitmqConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        this.rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            log.error("消息{}并没有到达队列，其交换机为{}，routingKey为{}",message,exchange,routingKey);
        });
        this.rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) -> {
            if (!ack) {
                log.error("消息并没有到达交换机，原因为{}", cause);
            }
        });
    }

    @Bean
    //返回一个90秒钟的延时队列
    public Queue delayCloseOrderQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","ORDER-EXCHANGE");
        arguments.put("x-dead-routing-key","ORDER.TTL");
        arguments.put("x-message-ttl",90000);
        return QueueBuilder.durable("OMS-TTL").withArguments(arguments).build();
    }

    @Bean
    public Binding bindingQueueAndExchange() {
        return new Binding("OMS-TTL", Binding.DestinationType.QUEUE,"ORDER-EXCHANGE","ORDER.CLOSE", null);
    }
}
