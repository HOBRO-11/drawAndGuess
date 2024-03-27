package com.drawandguess.producer;

import lombok.Getter;

@Getter
public enum TestPersonStatus {

    ERROR(null),
    ONLINE(10),
    OFFLINE(20),
    PREPARED(30),
    WAITING(40),
    CANCELED(50);

    private final Integer code;

    TestPersonStatus(Integer code) {
        this.code = code;
    }

    public Integer code() {
        return code;
    }

}
