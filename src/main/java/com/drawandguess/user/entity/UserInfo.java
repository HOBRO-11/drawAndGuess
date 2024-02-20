package com.drawandguess.user.entity;

import com.drawandguess.model.BaseEntity;
import com.drawandguess.user.dto.UserJoinDto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserInfo extends BaseEntity {

    private String email;

    private String password;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserInfo(UserJoinDto userJoinDto) {
        this.email = userJoinDto.getMail();
        this.password = userJoinDto.getPassword();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserInfo(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
