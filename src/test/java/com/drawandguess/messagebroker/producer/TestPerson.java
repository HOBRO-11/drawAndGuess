package com.drawandguess.messagebroker.producer;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestPerson implements Serializable {

    @JsonProperty("name")
    private String name;
    @JsonProperty("age")
    private int age;
    @JsonProperty("content")
    private String content;
    @JsonProperty("createdTime")
    private LocalDateTime createdTime;
    @JsonProperty("temp")
    private String temp;
    @JsonProperty("status")
    private TestPersonStatus status;

}
