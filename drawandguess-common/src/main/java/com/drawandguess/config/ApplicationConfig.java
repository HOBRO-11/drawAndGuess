package com.drawandguess.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Validated
@Component
@Getter
@Setter
@PropertySource(value = "classpath:/application-common.yml")
@ConfigurationProperties(prefix = "")
public class ApplicationConfig {

    private String redisHost;
    private int redisPort;
    private String successUsersKey;
    private String failureUsersKey;
    private String waitingUsersStream;
    private String statusChangeUsersStream;
    private int streamPollTimeout;
    private String waitingUsersConsumerGroup;
    private String statusChangeUsersConsumerGroup;
    private String waitingUsersConsumer;
    private String statusChangeUsersConsumer;

}
