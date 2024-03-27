package com.drawandguess.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.lang.NonNull;

import com.drawandguess.serializer.CustomRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisMessageBrokerConfig {

    @Autowired
    ApplicationConfig config ;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        String host = config.getRedisHost();
        int port = config.getRedisPort();
        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(host, port));
    }

    @Bean
    public RedisTemplate<String, Object> customeRedisTemplate(@NonNull RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        CustomRedisSerializer customSerializer = new CustomRedisSerializer();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(customSerializer);
        redisTemplate.setHashValueSerializer(customSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

}
