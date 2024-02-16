package com.drawandguess.user.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.drawandguess.user.dto.NicknameTagDto;
import com.drawandguess.user.dto.UserJoinDto;
import com.drawandguess.user.entity.UserInfo;
import com.drawandguess.user.entity.UserProfile;
import com.drawandguess.user.repository.UserInfoRepository;
import com.drawandguess.user.repository.UserProfileRepository;
import com.drawandguess.user.repository.UserQueryRepository;

import jakarta.validation.Valid;

@Service
public class UserInfoProfileService {

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    UserQueryRepository userQueryRepository;

    // security를 적용 전
    //비밀번호 관련 추가 스펙 변경이 있을 수 있음
    @Transactional
    public void join(@Valid @ModelAttribute UserJoinDto userJoin) throws BadRequestException {
        String mail = userJoin.getMail();
        String password = userJoin.getPassword();

        UserProfile userProfile = new UserProfile("GUEST", findMaxGuestTag());
        UserInfo userInfo = new UserInfo(mail, password);

        //todo
        //password 검증코드 집어 넣기
        Optional<UserInfo> findByEmail = userInfoRepository.findByEmail(mail);
        if (findByEmail.isPresent()) {
            throw new BadRequestException("이미 존재하는 email입니다.");
        }

        userProfileRepository.save(userProfile);
        userInfo.setUserProfile(userProfile);
        userInfoRepository.save(userInfo);
    }

    private String findMaxGuestTag() {
        int maxGuestTag = userQueryRepository.findMaxGuestTag();
        return String.valueOf(maxGuestTag + 1);
    }

    //비밀번호 관련 추가 스펙 변경이 있을 수 있음
    @Transactional
    void changePassword(@NonNull Long id ,String password) throws BadRequestException {

        Optional<UserInfo> findUserInfo = userInfoRepository.findById(id);
        if (findUserInfo.isEmpty()) {
            throw new BadRequestException("해당 유저는 존재하지 않습니다.");
        }
        findUserInfo.get().setPassword(password);
    }

    @Transactional
    void changeNicknameAndTag(@NonNull Long id, NicknameTagDto nicknameTagDto) throws BadRequestException {
        
        String nickname = nicknameTagDto.getNickname();
        String tag = nicknameTagDto.getTag();

        UserProfile findInfo = userProfileRepository.findById(id).orElseThrow(() -> new BadRequestException("해당 유저는 존재하지 않습니다."));

        boolean isPossible = isThisNicknamePossible(nickname);
        if (!isPossible) {
            throw new BadRequestException("Guest로 시작하는 nickname 또는 5글자 이하의 nickname 사용할 수 없습니다.");
        }

        boolean canModified = canModified(findInfo);
        if (!canModified) {
            throw new BadRequestException("변경일로부터 90일 이후에 변경 가능합니다.");
        }

        Optional<UserProfile> findUserProfile = userProfileRepository.findByNicknameAndTag(nickname, tag);
        if (findUserProfile.isPresent()) {
            throw new BadRequestException("이미 존재하는 tag입니다.");
        }

        findInfo.setNickname(nickname);
        findInfo.setTag(tag);

    }

    private boolean canModified(UserProfile userProfile) {

        String nickname = userProfile.getNickname();
        LocalDateTime lastModifiedAt = userProfile.getLastModifiedAt();
        Long duration = Duration.between(lastModifiedAt, LocalDateTime.now()).toDays();

        if (nickname.equals("GUEST")) {
            return true;
        }

        if (duration < 90L) {
            return false;
        }

        return true;
    }

    private boolean isThisNicknamePossible(String newNickname) {

        char[] nickname = newNickname.toCharArray();
        char[] capitalGuest = { 'G', 'U', 'E', 'S', 'T' };
        char[] subGuest = { 'g', 'u', 'e', 's', 't' };
        int count = 0;

        if (nickname.length <= 5) {
            return false;
        }

        for (int i = 0; i < 5; i++) {
            if ((nickname[i] == capitalGuest[i]) || (nickname[i] == subGuest[i])) {
                count += 1;
            } else {
                break;
            }
        }

        return count != 5;
    }

    public void deleteUser(@NonNull Long id){
        userInfoRepository.deleteById(id);
    }
}
