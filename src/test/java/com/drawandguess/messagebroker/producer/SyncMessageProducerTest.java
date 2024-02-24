package com.drawandguess.messagebroker.producer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.drawandguess.messagebroker.consumer.SyncMessageConsumer;

@SpringBootTest
public class SyncMessageProducerTest {

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    SyncMessageConsumer messageConsumer;
    
    @Test
    void testProduce() throws InterruptedException {
        messageProducer.streamStart();
        messageConsumer.createGroup("s1", "g1");
        Thread.sleep(1000L);
        messageProducer.produce();
        Thread.sleep(1000L);
        messageConsumer.syncConsume("s1","g1","u1");
        messageConsumer.syncRead("s1");
    }
}
