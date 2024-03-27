package com.drawandguess.dto;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public abstract class MessageDto {

    private long id;

    private LocalDateTime requestTime;

    public void setId(long id){
        this.id = id;
    }

    public MessageDto() {
        this.requestTime = LocalDateTime.now();
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    



}
