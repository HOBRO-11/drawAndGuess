package com.drawandguess.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.drawandguess.dto.NicknameTagDto;
import com.drawandguess.dto.UserJoinDto;
import com.drawandguess.entity.UserInfo;
import com.drawandguess.entity.UserProfile;
import com.drawandguess.repository.UserInfoRepository;
import com.drawandguess.repository.UserProfileRepository;
import com.drawandguess.repository.UserQueryRepository;

import jakarta.validation.Valid;

@Service
public class UserInfoProfileService {

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    UserQueryRepository userQueryRepository;

    final static private String GUEST_NICKNAME = "GUEST";
    final static private long DATE_OF_MODIFICATION_AVAILABLE = 90L;

    @Transactional
    public void join(@Valid @ModelAttribute UserJoinDto userJoin) throws BadRequestException {
        
        validationEmail(userJoin);
        
        UserProfile guestProfile = getGuestUserProfile();
        userProfileRepository.save(guestProfile);
        UserInfo guestInfo = new UserInfo(userJoin);
        guestInfo.setUserProfile(guestProfile);
        userInfoRepository.save(guestInfo);
    }

    private @NonNull UserProfile getGuestUserProfile() {
        UserProfile userProfile = new UserProfile(GUEST_NICKNAME, findMaxGuestTag());
        return userProfile;
    }

    private void validationEmail(UserJoinDto userJoinDto) throws BadRequestException {
        Optional<UserInfo> findByEmail = userInfoRepository.findByEmail(userJoinDto.getMail());
        if (findByEmail.isPresent()) {
            throw new BadRequestException("이미 존재하는 email입니다.");
        }
    }

    private String findMaxGuestTag() {
        int maxGuestTag = userQueryRepository.findMaxGuestTag();
        return String.valueOf(maxGuestTag + 1);
    }

    @Transactional
    void changePassword(@NonNull Long id, String password) throws BadRequestException {

        Optional<UserInfo> findUserInfo = userInfoRepository.findById(id);

        validationExistUser(findUserInfo);

        findUserInfo.get().setPassword(password);
    }

    private void validationExistUser(Optional<UserInfo> findUserInfo) throws BadRequestException {
        if (findUserInfo.isEmpty()) {
            throw new BadRequestException("해당 유저는 존재하지 않습니다.");
        }
    }

    @Transactional
    void changeNicknameAndTag(@NonNull Long id, NicknameTagDto nicknameTagDto) throws BadRequestException {

        String nickname = nicknameTagDto.getNickname();
        String tag = nicknameTagDto.getTag();

        UserProfile findUserProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("해당 유저는 존재하지 않습니다."));

        validationNickname(nicknameTagDto);
        validationDurationModified(findUserProfile);
        validationTag(nicknameTagDto);

        findUserProfile.setNickname(nickname);
        findUserProfile.setTag(tag);

    }

    private void validationTag(NicknameTagDto nicknameTagDto) throws BadRequestException {

        String nickname = nicknameTagDto.getNickname();
        String tag = nicknameTagDto.getTag();

        Optional<UserProfile> findUserProfile = userProfileRepository.findByNicknameAndTag(nickname, tag);
        if (findUserProfile.isPresent()) {
            throw new BadRequestException("이미 존재하는 tag입니다.");
        }

    }

    private void validationDurationModified(UserProfile userProfile) throws BadRequestException {

        String nickname = userProfile.getNickname();
        LocalDateTime lastModifiedAt = userProfile.getLastModifiedAt();
        Long duration = Duration.between(lastModifiedAt, LocalDateTime.now()).toDays();

        if (nickname.equals("GUEST")) {
            return;
        }

        if (duration < DATE_OF_MODIFICATION_AVAILABLE) {
            throw new BadRequestException(
                    "변경일로부터 90일 이후에 변경이 가능합니다. " + (DATE_OF_MODIFICATION_AVAILABLE - duration) + "일 이후에 변경 가능합니다.");
        }

    }

    private void validationNickname(NicknameTagDto nicknameTagDto) throws BadRequestException {

        char[] nickname = nicknameTagDto.getNickname().toCharArray();
        char[] capitalGuest = { 'G', 'U', 'E', 'S', 'T' };
        char[] subGuest = { 'g', 'u', 'e', 's', 't' };

        if (nickname.length <= 5) {
            throw new BadRequestException("5글자 이하의 닉네임은 사용할 수 없습니다.");
        }

        for (int i = 0; i < 5; i++) {
            if ((nickname[i] == capitalGuest[i]) || (nickname[i] == subGuest[i])) {
            } else {
                return;
            }
        }
        throw new BadRequestException("Guest로 시작하는 닉네임은 사용할 수 없습니다.");
    }

    public void deleteUser(@NonNull Long id) {
        userInfoRepository.deleteById(id);
    }
}
