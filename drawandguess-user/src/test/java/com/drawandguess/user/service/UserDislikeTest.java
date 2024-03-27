package com.drawandguess.user.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.drawandguess.entity.UserDislike;
import com.drawandguess.entity.UserProfile;
import com.drawandguess.repository.UserDislikeRepository;
import com.drawandguess.repository.UserProfileRepository;
import com.drawandguess.repository.UserQueryRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

@Transactional
@SpringBootTest
@Slf4j
public class UserDislikeTest {

    @Autowired
    UserDislikeRepository userDislikeRepository;

    
    @Autowired
    UserProfileRepository userProfileRepository;
    
    @Autowired
    UserQueryRepository userQueryRepository;

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    void beforeEach() {
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
    }

    @AfterEach
    void afterEach() {
        userProfileRepository.deleteAll();
        userDislikeRepository.deleteAll();
    }

    @Test
    void addDislike_dislike_cu_success() {
        // given
        UserProfile findUser1L = userProfileRepository.findById(1L).get();
        UserProfile findUser3L = userProfileRepository.findById(3L).get();
        String newNickname = "newNickname";
        String newTag = "newTag";

        // when
        Optional<UserDislike> findDislike = userDislikeRepository.findByUserProfileAndDislikeUser(findUser1L,
                findUser3L);

        // 기존 유저가 nickname and tag를 변경함
        if (findDislike.isPresent()) {
            UserDislike userDislike = findDislike.get();
            userDislike.setUserDislikeNickname(newNickname);
            userDislike.setUserDislikeTag(newTag);
        }
        // dislike list에 없는 유저를 새로 추가
        else {
            UserDislike userDislike = new UserDislike(findUser1L, findUser3L);
            userDislikeRepository.save(userDislike);
        }

        // then
        
        Optional<UserDislike> assertDislike = userDislikeRepository.findByUserProfileAndDislikeUser(findUser1L,
                findUser3L);
        assertDislike.get();
        assertThat(assertDislike.get().getUserDislikeNickname()).isEqualTo(newNickname);
        assertThat(assertDislike.get().getUserDislikeTag()).isEqualTo(newTag);
    }

    @Test
    void deleteDislike_dislike_d_success() {
        // when
        UserProfile findUser = new UserProfile();
        findUser.setId(1L);
        List<UserDislike> userDislikes = userDislikeRepository.findByUserProfile(findUser);

        List<UserDislike> removed = new ArrayList<>();
        userDislikes.forEach(t -> {
            if (t.getUserDislikeNickname().equals("GUEST") && t.getUserDislikeTag().equals("2"))
                removed.add(t);
        });
        userDislikeRepository.deleteAll(removed);

        // then
        List<UserDislike> results = userDislikeRepository.findByUserProfile(findUser);
        List<String> collects = results.stream().map(t -> t.getUserDislikeNickname() + t.getUserDislikeTag())
                .collect(Collectors.toList());
        assertThat(collects.contains("GUEST2")).isFalse();
    }

    @Test
    @Transactional
    @Rollback(true)
    void deleteDislike_dislike_d_list_success() {
        // given

        UserProfile findUser = new UserProfile();
        findUser.setId(1L);
        List<UserDislike> findUserDislikes = userDislikeRepository.findByUserProfile(findUser);
        
        UserProfile dislikeUser1 = new UserProfile();
        dislikeUser1.setId(2L);
        UserProfile dislikeUser2 = new UserProfile();
        dislikeUser1.setId(3L);
        
        List<Long> wannaDislikeUserIds = new ArrayList<>();
        wannaDislikeUserIds.add(dislikeUser1.getId());
        wannaDislikeUserIds.add(dislikeUser2.getId());

        // when

        List<UserDislike> deleteUserDislikes = findUserDislikes.stream().filter(t -> wannaDislikeUserIds.contains(t.getId())).collect(Collectors.toList());

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
