package com.drawandguess.user.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.drawandguess.user.entity.UserDislike;
import com.drawandguess.user.entity.UserProfile;
import com.drawandguess.user.repository.UserDislikeRepository;
import com.drawandguess.user.repository.UserProfileRepository;
import com.drawandguess.user.repository.UserQueryRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class UserDislikeTest {

    @Autowired
    UserDislikeRepository userDislikeRepository;

    @Autowired
    UserQueryRepository userQueryRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    @Transactional
    void addDislike_dislike_cu_success() {
        // given
        UserProfile userProfile = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(userProfile);

        UserProfile dislikeUser1 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser1);

        UserProfile dislikeUser2 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser2);

        UserProfile dislikeUser3 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser3);

        UserProfile dislikeUser4 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser4);

        UserDislike userDislike1 = new UserDislike(userProfile, dislikeUser1);
        userDislikeRepository.save(userDislike1);

        UserDislike userDislike2 = new UserDislike(userProfile, dislikeUser2);
        userDislikeRepository.save(userDislike2);

        UserDislike userDislike3 = new UserDislike(userProfile, dislikeUser3);
        userDislikeRepository.save(userDislike3);

        UserProfile findUser = userProfileRepository.findById(3L).get();
        UserProfile assertUser = findUser;
        assertUser.setNickname("nickname");
        assertUser.setTag("tag");

        // when
        Optional<UserDislike> findDislike = userDislikeRepository.findByUserProfileAndDislikeUser(userProfile,
                assertUser);

        // 기존 유저가 nickname and tag를 변경함
        if (findDislike.isPresent()) {
            UserDislike userDislike = findDislike.get();
            userDislike.setUserDislikeNickname(assertUser.getNickname());
            userDislike.setUserDislikeTag(assertUser.getTag());
        }
        // dislike list에 없는 유저를 새로 추가
        else {
            UserDislike userDislike = new UserDislike(userProfile, assertUser);
            userDislikeRepository.save(userDislike);
        }

        // then
        Optional<UserDislike> assertDislike = userDislikeRepository.findByUserProfileAndDislikeUser(userProfile,
                assertUser);
        assertDislike.get();
        assertThat(assertDislike.get().getUserDislikeNickname()).isEqualTo("nickname");
        assertThat(assertDislike.get().getUserDislikeTag()).isEqualTo("tag");
    }

    @Test
    @Transactional
    void deleteDislike_dislike_d_success() {
        // given
        UserProfile userProfile = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(userProfile);

        UserProfile dislikeUser1 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser1);

        UserProfile dislikeUser2 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser2);

        UserProfile dislikeUser3 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser3);

        UserDislike userDislike1 = new UserDislike(userProfile, dislikeUser1);
        userDislikeRepository.save(userDislike1);

        UserDislike userDislike2 = new UserDislike(userProfile, dislikeUser2);
        userDislikeRepository.save(userDislike2);

        UserDislike userDislike3 = new UserDislike(userProfile, dislikeUser3);
        userDislikeRepository.save(userDislike3);

        // when
        UserProfile wannaFindUser = new UserProfile();
        wannaFindUser.setId(1L);
        List<UserDislike> userDislikes = userDislikeRepository.findByUserProfile(userProfile);

        List<UserDislike> removed = new ArrayList<>();
        userDislikes.forEach(t -> {
            if (t.getUserDislikeNickname().equals("GUEST") && t.getUserDislikeTag().equals("2"))
                removed.add(t);
        });
        userDislikeRepository.deleteAll(removed);

        // then
        List<UserDislike> results = userDislikeRepository.findByUserProfile(userProfile);
        List<String> collects = results.stream().map(t -> t.getUserDislikeNickname() + t.getUserDislikeTag())
                .collect(Collectors.toList());
        assertThat(collects.contains("GUEST2")).isFalse();
    }

    @Test
    @Transactional
    void deleteDislike_dislike_d_list_success() {
        // given
        UserProfile userProfile = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(userProfile);

        UserProfile dislikeUser1 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser1);

        UserProfile dislikeUser2 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser2);

        UserProfile dislikeUser3 = new UserProfile("GUEST", findMaxGuestTag());
        userProfileRepository.save(dislikeUser3);

        UserDislike userDislike1 = new UserDislike(userProfile, dislikeUser1);
        userDislikeRepository.save(userDislike1);

        UserDislike userDislike2 = new UserDislike(userProfile, dislikeUser2);
        userDislikeRepository.save(userDislike2);

        UserDislike userDislike3 = new UserDislike(userProfile, dislikeUser3);
        userDislikeRepository.save(userDislike3);

        List<UserProfile> dislikeUserProfiles = new ArrayList<>();
        dislikeUserProfiles.add(dislikeUser2);
        dislikeUserProfiles.add(dislikeUser3);

        List<Long> wannaDislikeUserId = new ArrayList<>();
        wannaDislikeUserId.add(dislikeUser2.getId());
        wannaDislikeUserId.add(dislikeUser3.getId());

        // when

        List<UserDislike> findUserDislikes = userDislikeRepository.findByUserProfile(userProfile);

        List<UserDislike> deleteUserDislikes = findUserDislikes.stream()
                .filter(t -> wannaDislikeUserId.contains(t.getDislikeUser().getId())).collect(Collectors.toList());

        if (deleteUserDislikes.size() > 0) {
            userDislikeRepository.deleteAll(deleteUserDislikes);
        }
        
        //then
        assertThat(userDislikeRepository.findAll().get(0).getUserDislikeTag()).isEqualTo("2");

    }

    private String findMaxGuestTag() {
        int maxGuestTag = userQueryRepository.findMaxGuestTag();
        return String.valueOf(maxGuestTag + 1);
    }

}
