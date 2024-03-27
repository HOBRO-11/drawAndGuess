package com.drawandguess.producer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootTest
@SuppressWarnings("unchecked")
public class AsyncMessageProducerTest {

    @Autowired
    RedisConnectionFactory redisConnectionFactory;
    private ObjectMapper mapper;
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void beforeEach() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        TestCustomRedisSerializer testSerializer = new TestCustomRedisSerializer();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        redisTemplate.setHashKeySerializer(testSerializer);
        redisTemplate.setHashValueSerializer(testSerializer);

        redisTemplate.afterPropertiesSet();
    }

    @Test
    void producerCustomSerializerTest_same_Dto() throws JsonProcessingException, InterruptedException {
        LocalDateTime now = LocalDateTime.now();
        TestPerson person = new TestPerson("user1", 10, "10", now, null, TestPersonStatus.ONLINE);

        Map<String, Object> map;
        map = mapper.convertValue(person, Map.class);

        redisTemplate.opsForStream().add("s1", map);

        List<MapRecord<String, Object, Object>> list = redisTemplate.opsForStream().read(StreamReadOptions.empty(),
                StreamOffset.create("s1", ReadOffset.from("0")));

        MapRecord<String, Object, Object> mapRecord = list.get(list.size() - 1);

        Map<Object, Object> value = mapRecord.getValue();
        TestPerson result = mapper.convertValue(value, TestPerson.class);

        assertThat(result.getName()).isEqualTo(person.getName());
        assertThat(result.getAge()).isEqualTo(person.getAge());
        assertThat(result.getContent()).isEqualTo(person.getContent());
        assertThat(result.getCreatedTime()).isEqualTo(now);
        assertThat(result.getStatus()).isEqualTo(TestPersonStatus.ONLINE);

    }

    @Test
    void producerCustomSerializerTest_other_Dto() throws JsonProcessingException, InterruptedException {

        final String TEST_MOCK_PERSON_AGE = "10";

        LocalDateTime now = LocalDateTime.now();
        TestPerson person = new TestPerson("user1", 10, "10", now, null, TestPersonStatus.OFFLINE);

        Map<String, Object> map = mapper.convertValue(person, Map.class);

        redisTemplate.opsForStream().add("s1", map);

        List<MapRecord<String, Object, Object>> list = redisTemplate.opsForStream().read(StreamReadOptions.empty(),
                StreamOffset.create("s1", ReadOffset.from("0")));

        MapRecord<String, Object, Object> mapRecord = null;

        for (MapRecord<String, Object, Object> record : list) {
            if (!record.getValue().isEmpty()) {
                mapRecord = record;
            }
        }
        
        Map<Object, Object> value = mapRecord.getValue();

        // 다른 같은 이름의 필드를 사용하지만 모든 필드의 타입이 String인 다른 DTO로 convert 됨
        TestMockPerson result = mapper.convertValue(value, TestMockPerson.class);

        assertThat(result.getName()).isEqualTo(person.getName());
        assertThat(result.getAge()).isNotEqualTo(person.getAge());
        assertThat(result.getAge()).isEqualTo(TEST_MOCK_PERSON_AGE);
        assertThat(result.getContent()).isEqualTo(person.getContent());
        assertThat(result.getCreatedTime()).isEqualTo(now);
        assertThat(result.getStatus()).isEqualTo(person.getStatus().name());

    }

}
