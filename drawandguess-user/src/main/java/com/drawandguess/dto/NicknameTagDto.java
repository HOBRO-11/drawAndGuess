package com.drawandguess.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NicknameTagDto {

    @NotNull
    private String nickname;

    @NotNull
    private String tag;

	public NicknameTagDto(@NotNull String nickname, @NotNull String tag) {
		this.nickname = nickname;
		this.tag = tag;
	}


}
