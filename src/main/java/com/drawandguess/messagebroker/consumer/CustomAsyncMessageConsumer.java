package com.drawandguess.messagebroker.consumer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

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

import com.drawandguess.messagebroker.dto.GameMessage;
import com.drawandguess.serializer.CustomRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAsyncMessageConsumer
        implements StreamListener<String, MapRecord<String, Object, Object>>, InitializingBean, DisposableBean {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private ObjectMapper mapper;
    private StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> listenerContainer;
    private Subscription subscription;
    private String consumerName;
    private String consumerGroupName;
    private String streamName;

    @Override
    public void onMessage(MapRecord<String, Object, Object> message) {

        try {
            Map<Object, Object> value = message.getValue();
            GameMessage convertValue = mapper.convertValue(value, GameMessage.class);
log.info("consumerGroupName");
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            redisTemplate.opsForStream().acknowledge(streamName, consumerGroupName, message.getId());
        } catch (Exception e) {
            log.error("failed to process the message : {}", message.getValue().get("number"), e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        CustomRedisSerializer customRedisSerializer = new CustomRedisSerializer();

        consumerName = "su1";
        consumerGroupName = "sg1";
        streamName = "ss1";

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
                        .keySerializer(stringRedisSerializer)
                        .hashValueSerializer(stringRedisSerializer)
                        .hashKeySerializer(customRedisSerializer)
                        .hashValueSerializer(customRedisSerializer)
                        .pollTimeout(Duration.ofMillis(500L))
                        .build());

        this.subscription = listenerContainer.receive(Consumer.from(consumerGroupName, consumerName),
                StreamOffset.create(streamName, ReadOffset.lastConsumed()), this);

        subscription.await(Duration.ofSeconds(2));
        listenerContainer.start();
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

    public void stop() throws InterruptedException {
        this.subscription.cancel();
        this.subscription.await(Duration.ofSeconds(2));
    }

}
