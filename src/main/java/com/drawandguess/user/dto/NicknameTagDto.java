package com.drawandguess.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NicknameTagDto {

    @NotNull
    private String nickname;

    @NotNull
    private String tag;
}
