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
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.Subscription;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class AsyncMessageProducerTest {

        private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

        @Autowired
        RedisConnectionFactory connectionFactory;

        @Autowired
        StringRedisTemplate redisTemplate;

        @BeforeEach
        void before() {
                RedisConnection connection = connectionFactory.getConnection();
                connection.flushDb();
                connection.close();

        }

        @Test
        void AsyncMessageBrokerTest() throws InterruptedException {
                StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> containerOptions = StreamMessageListenerContainerOptions
                                .builder().pollTimeout(Duration.ofMillis(100)).build();

                BlockingQueue<MapRecord<String, String, String>> records = new LinkedBlockingQueue<>();

                StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = StreamMessageListenerContainer
                                .create(connectionFactory, containerOptions);

                redisTemplate.opsForStream().createGroup("my-stream", "my-group");

                container.start();
                Subscription subscription = container.receive(Consumer.from("my-group", "my-consumer"),
                                StreamOffset.create("my-stream", ReadOffset.lastConsumed()), records::add);

                subscription.await(DEFAULT_TIMEOUT);

                redisTemplate.opsForStream().add("my-stream", Collections.singletonMap("key", "value1"));

                MapRecord<String, String, String> msg = records.poll(1, TimeUnit.SECONDS);
                log.info("id : {}, key : {}, value : {}", msg.getId(), msg.getStream(), msg.getValue());

                subscription.cancel();
        }

}
