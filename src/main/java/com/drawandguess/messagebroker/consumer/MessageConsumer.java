package com.drawandguess.messagebroker.consumer;

import java.util.List;

import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo.XInfoGroups;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import io.lettuce.core.RedisCommandExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class MessageConsumer {

    private final RedisTemplate redisTemplate;

    public void createGroup() {
        boolean anyMatch = redisTemplate.opsForStream().groups("s1").stream().anyMatch(t -> t.groupName().equals("g1"));

        if (anyMatch) {
            return;
        }
        redisTemplate.opsForStream().createGroup("s1", "g1");
    }

    public void consume() {
        StreamOperations streamOperation = redisTemplate.opsForStream();
        List<MapRecord<String, String, String>> list = redisTemplate.opsForStream().read(Consumer.from("g1", "u1"),
                StreamReadOptions.empty().count(2),
                StreamOffset.create("s1", ReadOffset.lastConsumed()));
        log.info("stream size = {}", list.size() + "");
        list.stream()
                .forEach(t -> log.info("id : {}, stream : {}, value : {}", t.getId(), t.getStream(), t.getValue()));

    }
}
