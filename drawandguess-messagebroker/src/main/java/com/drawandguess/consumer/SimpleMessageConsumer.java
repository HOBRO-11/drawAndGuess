package com.drawandguess.consumer;

import java.time.Duration;
import java.util.Map;

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

import com.drawandguess.config.ApplicationConfig;
import com.drawandguess.dto.MessageDto;
import com.drawandguess.dto.SimpleMessageDto;
import com.drawandguess.serializer.CustomRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleMessageConsumer
        implements StreamListener<String, MapRecord<String, Object, Object>>, MessageConsumer {

    private final ApplicationConfig config;
    private final RedisTemplate<String, Object> redisTemplate;

    private StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> listenerContainer;
    private Subscription subscription;
    private ObjectMapper mapper;

    private String consumerName;
    private String consumerGroupName;
    private String streamName;

    @Override
    public void onMessage(MapRecord<String, Object, Object> message) {
        try {
            getMessage(message, SimpleMessageDto.class);
            redisTemplate.opsForStream().acknowledge(streamName, consumerGroupName, message.getId());
        } catch (Exception e) {
            log.error("failed to process the message : {}", message.getValue().get("number"), e);
        }
    }

    @Override
    public <T extends MessageDto> MessageDto getMessage(MapRecord<String, Object, Object> message,
            Class<T> returnType) {
        Map<Object, Object> value = message.getValue();
        SimpleMessageDto convertValue = mapper.convertValue(value, SimpleMessageDto.class);
        log.info("id : {}, content: {} , requestTime : {}", convertValue.getId(), convertValue.getContent(),
                convertValue.getRequestTime());
        return convertValue;
    }

    @Override
    @PostConstruct
    public void init() throws InterruptedException {

        consumerName = config.getWaitingUsersConsumer();
        consumerGroupName = config.getWaitingUsersConsumerGroup();
        streamName = config.getWaitingUsersStream();

        setObjectMapper();

        try {
            if (!redisTemplate.hasKey(streamName)) {
                log.info("해당 스트림 키가 존재하지 않습니다.생성하겠습니다. : {}", streamName);
                redisTemplate.opsForStream().createGroup(streamName, consumerGroupName);
            }
        } catch (Exception e) {
            log.info("해당 consumerGroup은 이미 존재합니다 : {}", consumerGroupName);
        }

        setListenerContainer();

        setSubscribe();

        subscription.await(Duration.ofSeconds(2));
        listenerContainer.start();
    }

    private void setSubscribe() {
        this.subscription = listenerContainer.receive(Consumer.from(consumerGroupName, consumerName),
                StreamOffset.create(streamName, ReadOffset.lastConsumed()), this);
    }

    private void setListenerContainer() {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        CustomRedisSerializer customRedisSerializer = new CustomRedisSerializer();

        this.listenerContainer = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(),
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .keySerializer(stringRedisSerializer)
                        .hashValueSerializer(stringRedisSerializer)
                        .hashKeySerializer(customRedisSerializer)
                        .hashValueSerializer(customRedisSerializer)
                        .pollTimeout(Duration.ofMillis(500L))
                        .build());
    }

    private void setObjectMapper() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    @PreDestroy
    public void destroy() {
        if (subscription != null) {
            subscription.cancel();
        }

        if (listenerContainer != null) {
            listenerContainer.stop();
        }
    }

}
