package com.drawandguess.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.drawandguess.user.repository.UserInfoRepository;
import com.drawandguess.user.repository.UserProfileRepository;
import com.drawandguess.user.repository.UserQueryRepository;

@Service
public class UserInfoProfileService {
    
    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    UserQueryRepository userQueryRepository;

}
