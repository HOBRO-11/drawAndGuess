package com.drawandguess.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class UserDislike  {
    
    @Id @GeneratedValue
    private Long id;

    private String userDislikeNickname;

    private String userDislikeTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Dislike_id")
    private UserProfile dislikeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Profile_id")
    private UserProfile userProfile;

    @Builder
	public UserDislike(String userDislikeNickname, String userDislikeTag, UserProfile dislikeUser,
			UserProfile userProfile) {
		this.userDislikeNickname = userDislikeNickname;
		this.userDislikeTag = userDislikeTag;
		this.dislikeUser = dislikeUser;
		this.userProfile = userProfile;
	}

    public UserDislike(UserProfile userProfile, UserProfile dislikeUser){
        this.userProfile = userProfile;
        this.dislikeUser = dislikeUser;
        this.userDislikeNickname = dislikeUser.getNickname();
        this.userDislikeTag = dislikeUser.getTag();
    }

    public void setUserDislikeNickname(String userDislikeNickname) {
        this.userDislikeNickname = userDislikeNickname;
    }

    public void setUserDislikeTag(String userDislikeTag) {
        this.userDislikeTag = userDislikeTag;
    }

}
