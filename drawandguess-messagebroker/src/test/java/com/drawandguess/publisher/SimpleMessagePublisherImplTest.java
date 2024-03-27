package com.drawandguess.publisher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.drawandguess.dto.SimpleMessageDto;

@SpringBootTest
public class SimpleMessagePublisherImplTest {

    @Autowired
    SimpleMessagePublisherImpl publisherImpl;

    @Test
    void testSendMessage() throws InterruptedException {
        SimpleMessageDto simpleMessageDto = new SimpleMessageDto("hello");
        publisherImpl.sendMessage(1L, simpleMessageDto);
        Thread.sleep(2000L);
    }
}
