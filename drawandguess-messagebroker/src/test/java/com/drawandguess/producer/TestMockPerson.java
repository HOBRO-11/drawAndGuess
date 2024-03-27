package com.drawandguess.producer;

import java.time.LocalDateTime;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestMockPerson implements Serializable {

    @JsonProperty("name")
    private String name;
    @JsonProperty("age")
    private String age;
    @JsonProperty("content")
    private String content;
    @JsonProperty("createdTime")
    private LocalDateTime createdTime;
    @JsonProperty("temp")
    private String temp;
    @JsonProperty("status")
    private String status;

}
