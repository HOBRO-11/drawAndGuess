package com.drawandguess.messagebroker.producer;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class AsyncMessageProducerTest {

    @Autowired
    RedisConnectionFactory testConnectionFactory;

    @Autowired
    StreamMessageListenerContainer<String , MapRecord<String , String , String >> container;

    @Autowired
    StringRedisTemplate redisTemplate;

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

    @BeforeEach
    void before() {

        redisTemplate.afterPropertiesSet();
        RedisConnection connection = testConnectionFactory.getConnection();
        connection.flushDb();
        connection.close();
    }

    @Test
    void testProduce() throws InterruptedException {
        BlockingQueue<MapRecord<String, String, String>> queue = new LinkedBlockingQueue<>();
        RecordId messageId = redisTemplate.opsForStream().add("my-stream", Collections.singletonMap("key", "value1"));
        redisTemplate.opsForStream().createGroup("my-stream", ReadOffset.from(messageId), "my-group");

        container.start();
        Subscription subscription = container.receive(Consumer.from("my-group", "my-consumer"),
                StreamOffset.create("my-stream", ReadOffset.lastConsumed()), queue::add);

        subscription.await(DEFAULT_TIMEOUT);

        redisTemplate.opsForStream().add("my-stream", Collections.singletonMap("key", "value2"));

        MapRecord<String, String, String> message = queue.poll(1, TimeUnit.SECONDS);
        

        log.info("id : {}, key : {}, value : {}", message.getId(), message.getStream(), message.getValue());

        // redisTemplate.opsForStream().acknowledge("my-group", message);
    }

}
