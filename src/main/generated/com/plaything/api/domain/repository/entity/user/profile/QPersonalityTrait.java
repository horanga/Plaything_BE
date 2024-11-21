package com.plaything.api.domain.repository.entity.user.profile;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPersonalityTrait is a Querydsl query type for PersonalityTrait
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPersonalityTrait extends EntityPathBase<PersonalityTrait> {

    private static final long serialVersionUID = 1110396876L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPersonalityTrait personalityTrait = new QPersonalityTrait("personalityTrait");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPrimaryTrait = createBoolean("isPrimaryTrait");

    public final QProfile profile;

    public final EnumPath<com.plaything.api.domain.user.constants.PersonalityTraitConstant> trait = createEnum("trait", com.plaything.api.domain.user.constants.PersonalityTraitConstant.class);

    public QPersonalityTrait(String variable) {
        this(PersonalityTrait.class, forVariable(variable), INITS);
    }

    public QPersonalityTrait(Path<? extends PersonalityTrait> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPersonalityTrait(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPersonalityTrait(PathMetadata metadata, PathInits inits) {
        this(PersonalityTrait.class, metadata, inits);
    }

    public QPersonalityTrait(Class<? extends PersonalityTrait> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.profile = inits.isInitialized("profile") ? new QProfile(forProperty("profile")) : null;
    }

}

