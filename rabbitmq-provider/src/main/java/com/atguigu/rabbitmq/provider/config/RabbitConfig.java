package com.atguigu.rabbitmq.provider.config;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        //确认消息是否到达交换机
        this.rabbitTemplate.setConfirmCallback((@Nullable CorrelationData correlationData, boolean ack, @Nullable String cause) -> {
            if(ack) {
                log.info("消息已经到达了交换机");
            } else {
                log.error("消息没有到达交换机，具体的原因是:{}", cause);
            }
        });

        //确认消息是否到达了队列
        this.rabbitTemplate.setReturnCallback((Message message, int replyCode, String replyText, String exchange, String routingKey) -> {
            log.error("消息没有到达队列，交换机为:{}，routingKey为:{}，消息为:{}" ,exchange,routingKey,message);
        });
    }

    //创建业务交换机
    @Bean
    public TopicExchange topicExchangeWork() {
        return new TopicExchange("spring-test-exchange2",true,false,null);
    }

    //常见业务队列（添加死信队列以及延时队列）
    @Bean
    public Queue queueWork() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","spring-test-exchange2");
        arguments.put("x-dead-letter-routing-key","msg.dead");
        arguments.put("x-message-ttl",30000);
        return new Queue("spring-test-queue2",true,false,false,arguments);
    }

    //将业务交换机和业务队列绑定起来
    @Bean
    public Binding bingWork(TopicExchange topicExchangeWork,Queue queueWork) {
        return BindingBuilder.bind(queueWork()).to(topicExchangeWork()).with("msg.work");
    }

    //创建死信队列
    @Bean
    public Queue queueDead() {
        return new Queue("spring-dead-queue",true,false,false,null);
    }

    //将死信队列和交换机绑定起来
    @Bean
    public Binding bindingDead(Queue queueDead,TopicExchange topicExchangeWork) {
        return BindingBuilder.bind(queueDead).to(topicExchangeWork).with("msg.dead");
    }
}

