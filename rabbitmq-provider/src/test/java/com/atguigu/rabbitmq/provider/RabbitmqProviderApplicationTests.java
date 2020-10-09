package com.atguigu.rabbitmq.provider;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RabbitmqProviderApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Test
    void contextLoads() {
        this.rabbitTemplate.convertAndSend("spring-test-exchange2","msg.work","hello my wife");
    }

}
