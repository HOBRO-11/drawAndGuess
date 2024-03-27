package com.drawandguess.publisher;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.drawandguess.config.ApplicationConfig;
import com.drawandguess.dto.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SimpleMessagePublisherImpl implements MessagePublisher {

    private final ApplicationConfig config;

    private final RedisTemplate<String, Object> template;

    private String streamKey;

    private ObjectMapper mapper;

    @Override
    @PostConstruct
    public void init() {
        this.streamKey = config.getWaitingUsersStream();

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void sendMessage(long id, MessageDto message) {
        Map<String, Object> map;
        message.setId(id);
        map = mapper.convertValue(message, Map.class);

        template.opsForStream().add(streamKey, map);
    }

}
