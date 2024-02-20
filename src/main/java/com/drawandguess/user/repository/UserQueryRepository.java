package com.drawandguess.user.repository;

import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Repository;

import com.drawandguess.user.dto.NicknameTagDto;
import com.drawandguess.user.entity.UserDislike;
import com.drawandguess.user.entity.UserProfile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

public interface UserQueryRepository {

    public List<NicknameTagDto> findTagByNicknameOrTag(NicknameTagDto userDto) throws BadRequestException;

    public int findMaxGuestTag();

    public void deleteUserDislikeUser(Long id, NicknameTagDto userNicknameTagDto);

    public Optional<UserDislike> findByUserProfileDislikeUser(UserProfile userProfile, UserProfile dislikeUser);

}
