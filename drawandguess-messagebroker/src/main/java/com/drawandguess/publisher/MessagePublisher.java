package com.drawandguess.publisher;

import com.drawandguess.dto.MessageDto;

public interface MessagePublisher {

    void init();
    public void sendMessage(long id, MessageDto message);

}
