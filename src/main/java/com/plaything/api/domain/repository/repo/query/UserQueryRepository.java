package com.plaything.api.domain.repository.repo.query;

import com.plaything.api.domain.repository.entity.user.ProfileImage;
import com.plaything.api.domain.repository.entity.user.profile.PersonalityTrait;
import com.plaything.api.domain.repository.entity.user.profile.Profile;
import com.plaything.api.domain.repository.entity.user.profile.RelationshipPreference;
import com.plaything.api.domain.user.model.request.MatchRequest;
import com.plaything.api.domain.user.model.response.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.plaything.api.domain.repository.entity.user.QProfileImage.profileImage;
import static com.plaything.api.domain.repository.entity.user.QUser.user;
import static com.plaything.api.domain.repository.entity.user.profile.QPersonalityTrait.personalityTrait;
import static com.plaything.api.domain.repository.entity.user.profile.QRelationshipPreference.relationshipPreference1;
import static com.plaything.api.domain.repository.entity.user.profile.QProfile.profile;

@RequiredArgsConstructor
@Repository
public class UserQueryRepository {

    private final static int LIMIT_OF_PARTNER = 10;
    private final JPAQueryFactory jpaQueryFactory;

    public List<UserMatching> searchUser(MatchRequest request){
        return jpaQueryFactory
                .selectFrom(profile)
                .innerJoin(profile.personalityTrait, personalityTrait)
                .innerJoin(profile.relationshipPreference, relationshipPreference1)
                .innerJoin(profile.profileImages, profileImage)
                .where(
                        profile.isBaned.isFalse().and(profile.nickName.ne(request.userName()))
                                .and(personalityTrait.trait.eq(request.personalityTraitConstant())
                                        .and(personalityTrait.isPrimaryTrait.isTrue()))
                )
                .limit(LIMIT_OF_PARTNER)
                .fetch()
                .stream()
                .map(p -> new UserMatching(
                        p.getPrimaryRole(),
                        p.getNickName(),
                        p.getBirthDate(),
                        p.getIntroduction(),
                        p.getPersonalityTrait().stream()
                                .map(trait -> new PersonalityTraitResponse(trait.getTrait(), trait.isPrimaryTrait()))
                                .collect(Collectors.toList()),
                        p.getRelationshipPreference().stream()
                                .map(pref -> new RelationshipPreferenceResponse(pref.getRelationshipPreference()))
                                .collect(Collectors.toList()),
                        p.getProfileImages().stream()
                                .map(img -> new ProfileImageResponse(img.getUrl()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }


}
