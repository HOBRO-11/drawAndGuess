package com.drawandguess.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drawandguess.user.entity.UserDislike;
import com.drawandguess.user.entity.UserProfile;

public interface UserDislikeRepository extends JpaRepository<UserDislike, Long>{

    List<UserDislike> findByUserProfile(UserProfile userProfile);

    Optional<UserDislike> findByUserProfileAndDislikeUser(UserProfile userProfile, UserProfile findUser3L);

}
