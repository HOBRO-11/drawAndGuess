package com.drawandguess.entity;


import com.drawandguess.model.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String nickname;

    private String tag;

    public UserProfile(String nickname, String tag) {
        this.nickname = nickname;
        this.tag = tag;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setId(Long id) {
        this.id = id;
    }



    
}
