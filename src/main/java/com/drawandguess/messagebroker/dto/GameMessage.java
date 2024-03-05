package com.drawandguess.messagebroker.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.drawandguess.user.entity.UserDislike;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameMessage {
    
    private String userNameTag;
    private Long userId;
    private List<UserDislike> userDislikes;
    private LocalDateTime requestTime;
}
