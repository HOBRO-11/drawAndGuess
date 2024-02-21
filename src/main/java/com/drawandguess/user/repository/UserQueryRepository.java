package com.drawandguess.user.repository;

import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;

import com.drawandguess.user.dto.NicknameTagDto;
import com.drawandguess.user.entity.UserDislike;
import com.drawandguess.user.entity.UserProfile;

public interface UserQueryRepository {

    public List<NicknameTagDto> findTagByNicknameOrTag(NicknameTagDto userDto) throws BadRequestException;

    public int findMaxGuestTag();

    public void deleteUserDislikeUser(Long id, NicknameTagDto userNicknameTagDto);

    public Optional<UserDislike> findByUserProfileDislikeUser(UserProfile userProfile, UserProfile dislikeUser);

}
