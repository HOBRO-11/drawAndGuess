package com.drawandguess.repository;

import java.util.List;
import java.util.Optional;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Repository;

import com.drawandguess.dto.NicknameTagDto;
import com.drawandguess.entity.UserDislike;
import com.drawandguess.entity.UserProfile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final EntityManager em;

    @Override
    public List<NicknameTagDto> findTagByNicknameOrTag(NicknameTagDto userDto) throws BadRequestException {
        String nickname = userDto.getNickname();
        String tag = userDto.getTag();

        StringBuilder jpqlSb = new StringBuilder(
                "select new com.drawandguess.user.user.service.dto.NicknameTagDto(up.nickname, up.tag) " +
                        "from UserProfile up " +
                        "where up.nickname = :nickname ");

        if (nickname == null) {
            throw new BadRequestException("반드시 닉네임은 포함되어야한다.");
        }

        if (tag != null) {
            String jpql = jpqlSb.append("and up.tag = :tag").toString();
            return em.createQuery(jpql, NicknameTagDto.class)
                    .setParameter("nickname", nickname)
                    .setParameter("tag", tag)
                    .getResultList();
        }

        String jpql = jpqlSb.toString();
        return em.createQuery(jpql, NicknameTagDto.class).setParameter("nickname", nickname).getResultList();

    }

    @Override
    public int findMaxGuestTag() {
        String jpql = "select MAX(up.tag)" +
                "from UserProfile up " +
                "where nickname = 'GUEST'";
        List<String> result = em.createQuery(jpql, String.class).getResultList();

        if (result.get(0) == null) {
            return 0;
        }

        return Integer.parseInt(result.get(0));
    }

    // todo
    @Override
    public void deleteUserDislikeUser(Long id, NicknameTagDto userNicknameTagDto) {
        String jpql = "delete from UserDislike ud " +
                "where ud.userDislikeNickname = :nickname" +
                "and " +
                "ud.userDislikeTag = :tag " +
                "and " +
                "ud.userProfile_id = :id ";

        em.createQuery(jpql)
                .setParameter("nickname", userNicknameTagDto.getNickname())
                .setParameter("tag", userNicknameTagDto.getTag())
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public Optional<UserDislike> findByUserProfileDislikeUser(UserProfile userProfile, UserProfile dislikeUser) {
        String jpql = "select ud " +
                "from UserDislike ud " +
                "where ud.userProfile.id = :userProfileId " +
                "and " +
                "ud.dislikeUser.id = :dislikeUserId ";

        try {
            UserDislike singleResult = em.createQuery(jpql, UserDislike.class)
                    .setParameter("userProfileId", userProfile.getId())
                    .setParameter("dislikeUserId", dislikeUser.getId())
                    .getSingleResult();
            return Optional.of(singleResult);
        } catch (NoResultException e) {
            return Optional.empty();
        }

    }

}
