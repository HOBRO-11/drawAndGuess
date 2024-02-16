package com.drawandguess.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserJoinDto {
    
    @Email
    private String mail;

    @NotNull
    private String  password;

}
