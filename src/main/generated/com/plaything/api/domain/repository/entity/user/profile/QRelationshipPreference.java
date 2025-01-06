package com.plaything.api.domain.repository.entity.user.profile;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRelationshipPreference is a Querydsl query type for RelationshipPreference
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRelationshipPreference extends EntityPathBase<RelationshipPreference> {

    private static final long serialVersionUID = 1427805119L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRelationshipPreference relationshipPreference1 = new QRelationshipPreference("relationshipPreference1");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QProfile profile;

    public final EnumPath<com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant> relationshipPreference = createEnum("relationshipPreference", com.plaything.api.domain.profile.constants.RelationshipPreferenceConstant.class);

    public QRelationshipPreference(String variable) {
        this(RelationshipPreference.class, forVariable(variable), INITS);
    }

    public QRelationshipPreference(Path<? extends RelationshipPreference> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRelationshipPreference(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRelationshipPreference(PathMetadata metadata, PathInits inits) {
        this(RelationshipPreference.class, metadata, inits);
    }

    public QRelationshipPreference(Class<? extends RelationshipPreference> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.profile = inits.isInitialized("profile") ? new QProfile(forProperty("profile")) : null;
    }

}

