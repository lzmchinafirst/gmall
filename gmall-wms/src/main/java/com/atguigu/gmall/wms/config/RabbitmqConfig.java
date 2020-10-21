package com.atguigu.gmall.wms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
        this.rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) -> {
            if(!ack) {
                log.error("消息没有到达交换机，原因为{}", cause);
            }
        });
        this.rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            log.error("消息没有到达队列，消息为{}，交换机为{}，routingKey为{}",message,exchange,routingKey);
        });
    }

    @Bean
    public Queue getReleaseStockQueue() {
        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","ORDER-EXCHANGE");
        arguments.put("x-dead-letter-routing-key","SEND.TTL");
        arguments.put("x-message-ttl",120000);
        return QueueBuilder.durable("TTL-QUEUE").withArguments(arguments).build();
    }

    @Bean
    public Binding getReleaseStockQueueBind() {
        return new Binding("STOCK-RELEASE-AUTO", Binding.DestinationType.QUEUE, "ORDER-EXCHANGE","SEND.DEAD", null);
    }
}
