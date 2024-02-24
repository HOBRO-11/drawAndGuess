package com.drawandguess.messagebroker.producer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.connection.SortParameters.Range;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {

    private final RedisTemplate redisTemplate;

    public void streamStart() {
        long size = redisTemplate.opsForStream().size("s1");
        if (size == 0L) {
            Map<String, String> map = new HashMap<>();
            map.put("sender", "producer");
            map.put("content", "start!");
            StringRecord record = StreamRecords.string(map).withStreamKey("s1");
            RecordId recordId = redisTemplate.opsForStream().add(record);
            log.info(record.getId().toString());
        }
    }

    public void produce() {

        for (int i = 0; i < 5; i++) {
            
            Map<String, String> map = new HashMap<>();
            map.put("sender", "producer" + i);
            map.put("content", "content" + i);
            redisTemplate.opsForStream().add("s1", map);
        }
    }
}
