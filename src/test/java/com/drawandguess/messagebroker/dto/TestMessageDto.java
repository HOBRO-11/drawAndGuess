package com.drawandguess.messagebroker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestMessageDto {
    
    private String sender;
    private String contents;
    
}
