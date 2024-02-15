package com.drawandguess.user.service;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.drawandguess.user.entity.UserInfo;
import com.drawandguess.user.entity.UserProfile;
import com.drawandguess.user.repository.UserInfoRepository;
import com.drawandguess.user.repository.UserProfileRepository;
import com.drawandguess.user.repository.UserQueryRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
public class UserInfoProfileTest {

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    UserQueryRepository userQueryRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @Transactional
    void Join_Info_Profile_c_success() throws BadRequestException {
        // given
        UserProfile userProfile = new UserProfile("GUEST", findMaxGuestTag());
        UserInfo userInfo = new UserInfo("email", "pwd");

        // when
        Optional<UserInfo> findByEmail = userInfoRepository.findByEmail("email");
        if (findByEmail.isPresent()) {
            throw new BadRequestException("이미 존재하는 email입니다.");
        }
        userProfileRepository.save(userProfile);
        userInfo.setUserProfile(userProfile);
        userInfoRepository.save(userInfo);

        // then
        assertThat(userInfoRepository.findByEmail("email").get().getUserProfile()).isEqualTo(userProfile);

    }

    private String findMaxGuestTag() {
        int maxGuestTag = userQueryRepository.findMaxGuestTag();
        return String.valueOf(maxGuestTag + 1);

    }

    @Test
    @Transactional
    void changePassword_Info_u_success() {
        // given
        UserProfile userProfile = new UserProfile("GUEST", findMaxGuestTag());
        UserInfo userInfo = new UserInfo("email", "pwd");

        userProfileRepository.save(userProfile);
        userInfo.setUserProfile(userProfile);
        userInfoRepository.save(userInfo);

        String newPwd = "newPwd";

        // when
        UserInfo findUserInfo = userInfoRepository.findByEmail("email").get();
        findUserInfo.setPassword(newPwd);

        // then
        assertThat(userInfoRepository.findByEmail("email").get().getPassword()).isEqualTo(newPwd);

    }

    @Test
    @Transactional
    void changeNicknameAndTag_profile_u_success() throws BadRequestException {
        // given
        UserProfile userProfile = new UserProfile("GUEST", findMaxGuestTag());
        UserInfo userInfo = new UserInfo("email", "pwd");

        userProfileRepository.save(userProfile);
        userInfo.setUserProfile(userProfile);
        userInfoRepository.save(userInfo);

        String nickname = "nickname";
        String tag = "tag";

        // when
        UserProfile save = userProfileRepository.findById(1L).get();

        boolean isPossible = isThisNicknamePossible(nickname);
        if (!isPossible) {
            throw new BadRequestException("Guest로 시작하는 nickname 또는 5글자 이하의 nickname 사용할 수 없습니다.");
        }

        boolean canModified = canModified(save);
        if (!canModified) {
            throw new BadRequestException("변경일로부터 90일 이후에 변경 가능합니다.");
        }

        Optional<UserProfile> findUserProfile = userProfileRepository.findByNicknameAndTag(nickname, tag);
        if (findUserProfile.isPresent()) {
            throw new BadRequestException("이미 존재하는 tag입니다.");
        }

        UserProfile findUser = userProfileRepository.findByNicknameAndTag("GUEST", "1").get();
        findUser.setNickname(nickname);
        findUser.setTag(tag);

        // then
        assertThat(userProfileRepository.findByNicknameAndTag(nickname, tag).get().getNickname()).isEqualTo(nickname);
        assertThat(userProfileRepository.findByNicknameAndTag(nickname, tag).get().getTag()).isEqualTo(tag);
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

    @Test
    @Transactional
    void deleteUser_Info_Profile_d_success(){
    //given
    UserProfile userProfile = new UserProfile("GUEST", findMaxGuestTag());
        UserInfo userInfo = new UserInfo("email", "pwd");

        userProfileRepository.save(userProfile);
        userInfo.setUserProfile(userProfile);
        userInfoRepository.save(userInfo);

    //when
    userInfoRepository.deleteById(1L);

    //then
    assertThat(userProfileRepository.findByNicknameAndTag("GUEST", "1")).isEmpty();
    assertThat(userInfoRepository.findById(1L)).isEmpty();
    }

}
