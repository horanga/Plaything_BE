package com.plaything.api.domain.repository.repo.query;

import com.plaything.api.domain.matching.model.request.MatchRequest;
import com.plaything.api.domain.matching.model.request.MatchRequestForOthers;
import com.plaything.api.domain.matching.model.response.UserMatching;
import com.plaything.api.domain.user.model.response.PersonalityTraitResponse;
import com.plaything.api.domain.user.model.response.ProfileImageResponse;
import com.plaything.api.domain.user.model.response.RelationshipPreferenceResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static com.plaything.api.domain.repository.entity.user.QProfileImage.profileImage;
import static com.plaything.api.domain.repository.entity.user.profile.QPersonalityTrait.personalityTrait;
import static com.plaything.api.domain.repository.entity.user.profile.QProfile.profile;
import static com.plaything.api.domain.repository.entity.user.profile.QRelationshipPreference.relationshipPreference1;

@RequiredArgsConstructor
@Repository
public class UserQueryRepository {

    private final static int LIMIT_OF_PARTNER = 10;
    private final JPAQueryFactory jpaQueryFactory;

    public List<UserMatching> searchUser(MatchRequest request) {
        return jpaQueryFactory
                .selectFrom(profile)
                .innerJoin(profile.personalityTrait, personalityTrait)
                .innerJoin(profile.relationshipPreference, relationshipPreference1)
                .innerJoin(profile.profileImages, profileImage)
                .where(personalityTrait.trait.eq(request.personalityTraitConstant())
                        .and(personalityTrait.isPrimaryTrait.isTrue()).and(profile.isPrivate.isFalse())
                        .and(profile.primaryRole.eq(request.primaryRole().getOpposite()))
                        .and(profile.isBaned.isFalse().and(profile.nickName.ne(request.userName()))))

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

    public List<UserMatching> searchUser(MatchRequestForOthers request) {
        return jpaQueryFactory
                .selectFrom(profile)
                .innerJoin(profile.personalityTrait, personalityTrait)
                .innerJoin(profile.relationshipPreference, relationshipPreference1)
                .innerJoin(profile.profileImages, profileImage)
                .where(profile.primaryRole.eq(request.primaryRole())
                        .and(profile.isBaned.isFalse().and(profile.nickName.ne(request.userName()))
                                .and(profile.isPrivate.isFalse())))
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
