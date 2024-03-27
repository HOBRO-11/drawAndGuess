package com.drawandguess.consumer;

import org.springframework.data.redis.connection.stream.MapRecord;

import com.drawandguess.dto.MessageDto;


public interface MessageConsumer {
    
    void init() throws InterruptedException;
    void destroy();
    <T extends MessageDto> MessageDto getMessage(MapRecord<String, Object , Object> message, Class<T> returnType);
}
