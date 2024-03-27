package com.drawandguess.dto;


public class SimpleMessageDto extends MessageDto{
    
    private String content;

    public SimpleMessageDto() {
    }

    public SimpleMessageDto(String content) {
        super();
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    
}
