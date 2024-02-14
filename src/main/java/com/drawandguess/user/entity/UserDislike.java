package com.drawandguess.user.entity;

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
    private UserProfile userDislike;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_Profile_id")
    private UserProfile userProfile;

    @Builder
	public UserDislike(String userDislikeNickname, String userDislikeTag, UserProfile userDislike,
			UserProfile userProfile) {
		this.userDislikeNickname = userDislikeNickname;
		this.userDislikeTag = userDislikeTag;
		this.userDislike = userDislike;
		this.userProfile = userProfile;
	}

    public void setUserDislikeNickname(String userDislikeNickname) {
        this.userDislikeNickname = userDislikeNickname;
    }

    public void setUserDislikeTag(String userDislikeTag) {
        this.userDislikeTag = userDislikeTag;
    }

}
