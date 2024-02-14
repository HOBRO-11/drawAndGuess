package com.drawandguess.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.drawandguess.user.entity.UserInfo;

public interface UserInfoRepository extends JpaRepository<UserInfo, Long>{

    Optional<UserInfo> findByEmail(String email);

    void deleteByEmail(String string);

}
