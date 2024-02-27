package com.drawandguess.messagebroker.consumer;

import java.time.Duration;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AsyncMessageConsumer
        implements StreamListener<String, MapRecord<String, String , String >>, InitializingBean, DisposableBean {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private Subscription subscription;
    private String consumerName;
    private String consumerGroupName;
    private String streamName;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {

        try {
            String number = message.getValue().get("number");
            log.info("inputNumber : {} time : {} thread : {}", number, System.currentTimeMillis(), Thread.currentThread().getId());
            redisTemplate.opsForStream().acknowledge(streamName, consumerGroupName, message.getId());
        } catch (Exception e) {
            log.error("failed to process the message : {}", message.getValue().get("number"), e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (subscription != null) {
            subscription.cancel();
        }

        if (listenerContainer != null) {
            listenerContainer.stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        consumerName = "u1";
        consumerGroupName = "g1";
        streamName = "s1";

        try {
            if (!redisTemplate.hasKey(streamName)) {
                log.info("해당 스트림 키가 존재하지 않습니다.생성하겠습니다. : {}", streamName);
                redisTemplate.opsForStream().createGroup(streamName, consumerGroupName);
            }
        } catch (Exception e) {
            log.info("해당 consumerGroup은 이미 존재합니다 : {}", consumerGroupName);
        }

        this.listenerContainer = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .serializer(new StringRedisSerializer())
                        .pollTimeout(Duration.ofMillis(500L))
                        .build());

        this.subscription = listenerContainer.receive(
                Consumer.from(consumerGroupName, consumerName),
                StreamOffset.create(streamName, ReadOffset.lastConsumed()),
                this);

        subscription.await(Duration.ofSeconds(2));
        listenerContainer.start();
    }

    public void stop() throws InterruptedException{
        this.subscription.cancel();
        this.subscription.await(Duration.ofSeconds(2));
    }

}
