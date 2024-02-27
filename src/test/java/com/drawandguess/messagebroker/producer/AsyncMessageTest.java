package com.drawandguess.messagebroker.producer;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import com.drawandguess.messagebroker.consumer.AsyncMessageConsumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class AsyncMessageTest {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    RedisTemplate<String, String> stringRedisTemplate;

    @Autowired
    AsyncMessageConsumer asyncMessageConsumer;

    @Autowired
    MessageProducer messageProducer;

    @org.junit.jupiter.api.BeforeEach
    void BeforeEach() {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.flushDb();
        connection.close();
    }

    @Test
    void test() throws InterruptedException {
        BlockingQueue<MapRecord<String, String, String>> queue = new LinkedBlockingQueue<>();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container = StreamMessageListenerContainer
                .create(redisTemplate.getConnectionFactory(),
                        StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                                .pollTimeout(Duration.ofMillis(100L))
                                .serializer(new StringRedisSerializer())
                                .build());

        redisTemplate.opsForStream().createGroup("s1", "g1");
        container.receive(Consumer.from("g1", "u1"), StreamOffset.create("s1",
                ReadOffset.lastConsumed()), queue::add);

        container.start();

        redisTemplate.opsForStream().add("s1", Collections.singletonMap("name", "user1"));
        redisTemplate.opsForStream().add("s1", Collections.singletonMap("name", "user2"));
        redisTemplate.opsForStream().add("s1", Collections.singletonMap("name", "user3"));
        redisTemplate.opsForStream().add("s1", Collections.singletonMap("name", "user4"));
        redisTemplate.opsForStream().add("s1", Collections.singletonMap("name", "user5"));

        MapRecord<String, String, String> msg = queue.poll(1, TimeUnit.SECONDS);
        log.info("id : {} stream : {} value : {}", msg.getId(), msg.getStream(),
                msg.getValue());

        container.stop();

    }

    @Test
    void ttest() {
        redisTemplate.opsForStream().createGroup("s1", ReadOffset.from("0"), "u1");
    }

}
