package com.drawandguess.messagebroker.producer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.drawandguess.messagebroker.consumer.MessageConsumer;

@SpringBootTest
public class MessageProducerTest {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    MessageConsumer messageConsumer;
    
    @Test
    void testProduce() throws InterruptedException {
        messageProducer.streamStart();
        messageConsumer.createGroup();
        Thread.sleep(1000L);
        messageProducer.produce();
        Thread.sleep(1000L);
        messageConsumer.consume();
    }
}
