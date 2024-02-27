package com.drawandguess.messagebroker.consumer;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

@SpringBootTest
public class AsyncMessageConsumerTest {

    AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(TestConfig.class);

    TestMessageConsumer messageConsumer = ac.getBean(TestMessageConsumer.class);

    RedisTemplate<String, String> redisTemplate = ac.getBean(RedisTemplate.class);

    PendingMessageScheduler pendingMessageScheduler = ac.getBean(PendingMessageScheduler.class);


    @BeforeEach
    void afterEach() throws InterruptedException {
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
        connection.flushDb();
        connection.close();
    }

    @Test
    void test() throws Exception {
        messageConsumer.afterPropertiesSet();
        redisTemplate.opsForStream().add("s1", Collections.singletonMap("number", "22"));
        redisTemplate.opsForStream().add("s1", Collections.singletonMap("number", "21"));

        Thread.sleep(5000L);
        pendingMessageScheduler.processPendingMessage();
        pendingMessageScheduler.processPendingMessage();
        messageConsumer.stop();
    }

    @Configuration
    static class TestConfig {

        private final static String redisHost = "localhost";
        private final static int redisPort = 6379;

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            return new LettuceConnectionFactory(
                    new RedisStandaloneConfiguration(redisHost, redisPort));
        }

        @Bean
        public RedisTemplate<String, String> redisTemplate() {
            RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
            redisTemplate.setConnectionFactory(redisConnectionFactory());
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            redisTemplate.setValueSerializer(new StringRedisSerializer());
            redisTemplate.afterPropertiesSet();
            return redisTemplate;
        }

        @Bean
        TestMessageConsumer testMessageConsumer(RedisTemplate<String, String> redisTemplate) throws Exception {
            return new TestMessageConsumer(redisTemplate);
        }

        @Bean
        PendingMessageScheduler pendingMessageScheduler(RedisTemplate<String, String> redisTemplate) {
            return new PendingMessageScheduler(redisTemplate);
        }
    }

    static class TestMessageConsumer
            implements StreamListener<String, MapRecord<String, Object, Object>>, DisposableBean {

        @Autowired
        RedisTemplate<String, String> redisTemplate;

        private StreamMessageListenerContainer<String, MapRecord<String, Object, Object>> listenerContainer;
        private Subscription subscription;
        private String consumerName;
        private String consumerGroupName;
        private String streamName;

        public TestMessageConsumer(RedisTemplate<String, String> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void onMessage(MapRecord<String, Object, Object> message) {

            try {
                String number = (String) message.getValue().get("number");
                System.out
                        .println("inputNumber : " + number + " time : " + System.currentTimeMillis() + " thread : "
                                + Thread.currentThread().getName());
                // redisTemplate.opsForStream().acknowledge(streamName, consumerGroupName, message.getId());
            } catch (Exception e) {
                System.out.println("failed to process the message : " + message.getValue().get("number")
                        + " message is " + e.getMessage());
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

        public void afterPropertiesSet() throws Exception {
            consumerName = "u2";
            consumerGroupName = "g1";
            streamName = "s1";

            try {
                if (!redisTemplate.hasKey(streamName)) {
                    System.out.println("해당 스트림 키가 존재하지 않습니다.생성하겠습니다. : " + streamName);
                    redisTemplate.opsForStream().createGroup(streamName, consumerGroupName);
                }
            } catch (Exception e) {
                System.out.println("해당 consumerGroup은 이미 존재합니다 : " + consumerGroupName);
            }

            this.listenerContainer = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(),
                    StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                            .hashKeySerializer(new JdkSerializationRedisSerializer())
                            .hashValueSerializer(new JdkSerializationRedisSerializer())
                            .pollTimeout(Duration.ofMillis(500L))
                            .build());

            this.subscription = listenerContainer.receive(
                    Consumer.from(consumerGroupName, consumerName),
                    StreamOffset.create(streamName, ReadOffset.lastConsumed()),
                    this);

            subscription.await(Duration.ofSeconds(2));
            listenerContainer.start();
        }

        public String getConsumerGroupName() {
            return consumerGroupName;
        }

        public void stop() throws InterruptedException {
            this.subscription.await(Duration.ofSeconds(1));
            this.subscription.cancel();
            this.subscription.await(Duration.ofSeconds(1));
        }

    }

    static class PendingMessageScheduler {
        private String consumerName;
        private String streamName;
        private String consumerGroupName;

        @Autowired
        RedisTemplate<String, String> redisTemplate;

        public PendingMessageScheduler(RedisTemplate<String, String> redisTemplate) {
			this.redisTemplate = redisTemplate;
		}

        public void processPendingMessage() {
            consumerName = "u2";
            streamName = "s1";
            consumerGroupName = "g1";

            PendingMessages messages = redisTemplate.opsForStream().pending(streamName,
                    consumerGroupName, Range.unbounded(), 1);

            for (PendingMessage message : messages) {
                claimMessage(message);
                processMessage(message);
            }
        }

        private void claimMessage(PendingMessage message) {
            List<MapRecord<String, Object, Object>> claim = redisTemplate.opsForStream().claim("s1", "g1", "u2",
                    Duration.ofMillis(20L), message.getId());
            System.out.println("Message: " + message.getIdAsString() + " has been claimed by " + consumerGroupName + ":"
                    + consumerName);
        }

        private void processMessage(PendingMessage pendingMessage) {
            List<MapRecord<String, Object, Object>> messagesToProcess = redisTemplate.opsForStream().range(streamName,
                    Range.closed(pendingMessage.getIdAsString(), pendingMessage.getIdAsString()));

            if (messagesToProcess == null || messagesToProcess.isEmpty()) {
                System.out.println(
                        "Message is not present. It has been either processed or deleted by some other process : "
                                + pendingMessage.getIdAsString());
            }
            try {
                MapRecord<String, Object, Object> message = messagesToProcess.get(0);
                String inputNumber = (String) message.getValue().get("number");
                final int number = Integer.parseInt(inputNumber);
                if (number % 2 == 0) {
                    redisTemplate.opsForList().rightPush("even-list", inputNumber);
                } else {
                    redisTemplate.opsForList().rightPush("odd-list", inputNumber);
                }
                redisTemplate.opsForHash().put("recordKey", "LAST_RESULT_HASH_KEY", number);
                redisTemplate.opsForHash().increment("recordKey", "PROCESSED_HASH_KEY", 1);
                redisTemplate.opsForHash().increment("recordKey", "RETRY_PROCESSED_HASH_KEY", 1);
                redisTemplate.opsForStream().acknowledge("g1", message);
                System.out.println("Message has been processed after retrying");
            } catch (Exception ex) {
                // log the exception and increment the number of errors count
                System.out.println(
                        "Failed to process the message: " + messagesToProcess.get(0).getValue().get("number") + ex);
                redisTemplate.opsForHash().increment("recordKey", "ERRORS_HASH_KEY", 1);
            }
        }
    }
}
