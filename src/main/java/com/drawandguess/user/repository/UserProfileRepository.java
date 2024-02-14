package com.drawandguess.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drawandguess.user.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByNicknameAndTag(String nickname, String tag);

}
