package com.drawandguess.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drawandguess.user.entity.UserDislike;
import com.drawandguess.user.entity.UserProfile;

public interface UserDislikeRepository extends JpaRepository<UserDislike, Long>{

    List<UserDislike> findByUserProfile(UserProfile userProfile);
    
}
