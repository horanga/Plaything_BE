package com.plaything.api.domain.repository.repo.query;

import static com.plaything.api.domain.matching.constants.MatchingConstants.KEYWORD_DUMMY_CACHE;
import static com.plaything.api.domain.repository.entity.profile.QPersonalityTrait.personalityTrait;
import static com.plaything.api.domain.repository.entity.profile.QProfile.profile;
import static com.plaything.api.domain.repository.entity.profile.QProfileImage.profileImage;
import static com.plaything.api.domain.repository.entity.profile.QRelationshipPreference.relationshipPreference1;
import static com.plaything.api.domain.repository.entity.user.QUser.user;

import com.plaything.api.domain.matching.model.request.MatchRequest;
import com.plaything.api.domain.matching.model.request.MatchRequestForOthers;
import com.plaything.api.domain.repository.entity.profile.Profile;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProfileQueryRepository {

  private final static int LIMIT_OF_PARTNER = 10;
  private final JPAQueryFactory jpaQueryFactory;

  public List<Profile> searchUser(MatchRequest request, List<String> candidateIds,
      List<String> matchingList, List<String> hideList) {

    BooleanBuilder booleanBuilder = getBooleanBuilder(candidateIds, matchingList, hideList);

    List<Profile> list = jpaQueryFactory
        .selectFrom(profile)
        .innerJoin(profile.personalityTrait, personalityTrait)
        .innerJoin(profile.relationshipPreference, relationshipPreference1)
        .innerJoin(profile.profileImages, profileImage)
        .innerJoin(profile.user, user)
        .where(personalityTrait.trait.eq(request.personalityTraitConstant())
            .and(personalityTrait.isPrimaryTrait.isTrue()).and(profile.isPrivate.isFalse())
            .and(profile.primaryRole.eq(request.primaryRole().getOpposite()))
            .and(profile.isBaned.isFalse().and(profile.nickName.ne(request.userName())))
            .and(profile.id.gt(request.lastId()))
            .and(booleanBuilder)
        )

        .limit(LIMIT_OF_PARTNER)
        .fetch();

    //재귀함수 무한 반복을 막기 위한 조건 설정 lastId>0
    if (list.isEmpty() && request.lastId() > 0) {
      MatchRequest newRequest = new MatchRequest(request.primaryRole(),
          request.personalityTraitConstant(), 0, request.userName());
      return searchUser(newRequest, candidateIds, matchingList, hideList);

    }

    return list;
  }

  public List<Profile> searchUserForOthers(MatchRequestForOthers request, List<String> candidateIds,
      List<String> matchingList, List<String> hideList) {
    BooleanBuilder booleanBuilder = getBooleanBuilder(candidateIds, matchingList, hideList);

    List<Profile> list = jpaQueryFactory
        .selectFrom(profile)
        .innerJoin(profile.personalityTrait, personalityTrait)
        .innerJoin(profile.relationshipPreference, relationshipPreference1)
        .innerJoin(profile.profileImages, profileImage)
        .innerJoin(profile.user, user)
        .where(profile.primaryRole.eq(request.primaryRole())
            .and(profile.isBaned.isFalse().and(profile.nickName.ne(request.userName()))
                .and(profile.isPrivate.isFalse()))
            .and(profile.id.gt(request.lastId()))
            .and(booleanBuilder))
        .limit(LIMIT_OF_PARTNER)
        .fetch();

    if (list.isEmpty() && request.lastId() > 0) {
      MatchRequestForOthers matchRequestForOthers = new MatchRequestForOthers(request.primaryRole(),
          0, request.userName());
      return searchUserForOthers(matchRequestForOthers, candidateIds, matchingList, hideList);

    }
    return list;
  }

  private BooleanBuilder getBooleanBuilder(List<String> loginIds, List<String> matchingList,
      List<String> hideList) {
    BooleanBuilder booleanBuilder = new BooleanBuilder();

    if (loginIds != null && !loginIds.isEmpty()) {
      if (!loginIds.get(0).equals(KEYWORD_DUMMY_CACHE)) {
        booleanBuilder.and(user.loginId.notIn(loginIds));
      }
    }

    if (matchingList != null && !matchingList.isEmpty()) {
      if (!matchingList.get(0).equals(KEYWORD_DUMMY_CACHE)) {
        booleanBuilder.and(user.loginId.notIn(matchingList));
      }
    }

    if (hideList != null && !hideList.isEmpty()) {
      if (!hideList.get(0).equals(KEYWORD_DUMMY_CACHE)) {
        booleanBuilder.and(user.loginId.notIn(hideList));
      }
    }

    return booleanBuilder;
  }
}
