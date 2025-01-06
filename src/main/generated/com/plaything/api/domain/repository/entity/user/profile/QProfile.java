package com.plaything.api.domain.repository.entity.user.profile;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProfile is a Querydsl query type for Profile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProfile extends EntityPathBase<Profile> {

    private static final long serialVersionUID = 520378525L;

    public static final QProfile profile = new QProfile("profile");

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final EnumPath<com.plaything.api.domain.profile.constants.Gender> gender = createEnum("gender", com.plaything.api.domain.profile.constants.Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath introduction = createString("introduction");

    public final BooleanPath isBaned = createBoolean("isBaned");

    public final BooleanPath isPrivate = createBoolean("isPrivate");

    public final StringPath nickName = createString("nickName");

    public final ListPath<PersonalityTrait, QPersonalityTrait> personalityTrait = this.<PersonalityTrait, QPersonalityTrait>createList("personalityTrait", PersonalityTrait.class, QPersonalityTrait.class, PathInits.DIRECT2);

    public final EnumPath<com.plaything.api.domain.profile.constants.PrimaryRole> primaryRole = createEnum("primaryRole", com.plaything.api.domain.profile.constants.PrimaryRole.class);

    public final ListPath<ProfileImage, QProfileImage> profileImages = this.<ProfileImage, QProfileImage>createList("profileImages", ProfileImage.class, QProfileImage.class, PathInits.DIRECT2);

    public final EnumPath<com.plaything.api.domain.profile.constants.ProfileStatus> profileStatus = createEnum("profileStatus", com.plaything.api.domain.profile.constants.ProfileStatus.class);

    public final ListPath<RelationshipPreference, QRelationshipPreference> relationshipPreference = this.<RelationshipPreference, QRelationshipPreference>createList("relationshipPreference", RelationshipPreference.class, QRelationshipPreference.class, PathInits.DIRECT2);

    public QProfile(String variable) {
        super(Profile.class, forVariable(variable));
    }

    public QProfile(Path<? extends Profile> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProfile(PathMetadata metadata) {
        super(Profile.class, metadata);
    }

}

