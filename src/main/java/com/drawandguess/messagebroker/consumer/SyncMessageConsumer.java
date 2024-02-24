package com.drawandguess.messagebroker.consumer;

import java.util.List;

import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class SyncMessageConsumer {

    private final @NonNull RedisTemplate redisTemplate;

    public void createGroup(String key, String group) {
        boolean anyMatch = redisTemplate.opsForStream()
                .groups(key)
                .stream()
                .anyMatch(t -> t.groupName().equals(group));
        if (anyMatch) {
            return;
        }
        redisTemplate.opsForStream()
                .createGroup(key, group);
    }

    public void syncConsume(String key, String group, String consumer) {
        StreamOperations streamOperation = redisTemplate.opsForStream();
        List<MapRecord<String, String, String>> list = redisTemplate.opsForStream()
                .read(Consumer.from(group, consumer),
                        StreamReadOptions.empty().count(2),
                        StreamOffset.create(key, ReadOffset.lastConsumed()));
        list.stream()
                .forEach(t -> log.info("id : {}, stream : {}, value : {}", t.getId(), t.getStream(), t.getValue()));
    }

    public void syncRead(String key) {
        List<MapRecord<String, String, String>> list = redisTemplate.opsForStream()
                .read(StreamReadOptions.empty().count(10), StreamOffset.fromStart(key));

        list.stream()
                .forEach(t -> log.info("list :  id : {}, stream : {}, value : {}", t.getId(), t.getStream(),
                        t.getValue()));
    }

    // public void ackStream() {
    //     redisTemplate.opsForStream().pen;
    // }

}
