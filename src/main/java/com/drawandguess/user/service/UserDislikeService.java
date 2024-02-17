package com.drawandguess.user.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.drawandguess.user.entity.UserDislike;
import com.drawandguess.user.entity.UserProfile;
import com.drawandguess.user.repository.UserDislikeRepository;
import com.drawandguess.user.repository.UserProfileRepository;
import com.drawandguess.user.repository.UserQueryRepository;

@Service
public class UserDislikeService {

    @Autowired
    UserDislikeRepository userDislikeRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    @Autowired
    UserQueryRepository userQueryRepository;

    @Transactional
    void addDislike(@NonNull Long id, @NonNull Long dislikeId) throws BadRequestException {
        // given

        UserProfile findUserProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("해당 유저가 존재하지 않습니다."));
        UserProfile wannaDislikeUser = userProfileRepository.findById(dislikeId)
                .orElseThrow(() -> new BadRequestException("해당 유저가 존재하지 않습니다."));

        // when
        Optional<UserDislike> findUserDislike = userDislikeRepository.findByUserProfileAndDislikeUser(findUserProfile,
                wannaDislikeUser);

        if (findUserDislike.isPresent()) {
            UserDislike userDislike = findUserDislike.get();
            userDislike.setUserDislikeNickname(wannaDislikeUser.getNickname());
            userDislike.setUserDislikeTag(wannaDislikeUser.getTag());
            return;
        }
        UserDislike userDislike = new UserDislike(findUserProfile, wannaDislikeUser);
        userDislikeRepository.save(userDislike);
    }

    @Transactional
    void deleteDislike(long id, long... dislikeUserId) {

        UserProfile wannaFindUser = new UserProfile();
        wannaFindUser.setId(id);
        List<UserDislike> findUserDislikes = userDislikeRepository.findByUserProfile(wannaFindUser);

        List<UserDislike> deleteUserDislikes = wannaDeleteUserDislikes(findUserDislikes, dislikeUserId);

        if(!deleteUserDislikes.isEmpty())
        userDislikeRepository.deleteAll(deleteUserDislikes);
    }

    private List<UserDislike> wannaDeleteUserDislikes(List<UserDislike> userDislikes, long[] dislikeUserIds) {

        if (userDislikes.size() == dislikeUserIds.length) {
            return userDislikes;
        }

        List<Long> wannaDislikeUserId = new ArrayList<>();

        for (long di : dislikeUserIds) {
            wannaDislikeUserId.add(di);
        }

        return userDislikes.stream()
                .filter(t -> wannaDislikeUserId.contains(t.getDislikeUser().getId())).collect(Collectors.toList());
    }

}
