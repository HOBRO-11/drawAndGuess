package com.drawandguess.messagebroker.producer;

import java.util.Collections;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageProducer {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    public void produce() {
        for (int i = 0; i < 5; i++) {
            Random random = new Random();
            int number = random.nextInt(2000);
            // Map<String, String> fields = new HashMap<>();
            String inputInt = String.valueOf(number);
            redisTemplate.opsForStream().add("s1", Collections.singletonMap("number", inputInt));
        }
    }
}
