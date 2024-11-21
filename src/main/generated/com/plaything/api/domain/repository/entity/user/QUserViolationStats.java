package com.plaything.api.domain.repository.entity.user;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserViolationStats is a Querydsl query type for UserViolationStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserViolationStats extends EntityPathBase<UserViolationStats> {

    private static final long serialVersionUID = 425768524L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserViolationStats userViolationStats = new QUserViolationStats("userViolationStats");

    public final NumberPath<Long> bannedImageCount = createNumber("bannedImageCount", Long.class);

    public final NumberPath<Long> bannedProfileCount = createNumber("bannedProfileCount", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> reportViolationCount = createNumber("reportViolationCount", Long.class);

    public final QUser user;

    public QUserViolationStats(String variable) {
        this(UserViolationStats.class, forVariable(variable), INITS);
    }

    public QUserViolationStats(Path<? extends UserViolationStats> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserViolationStats(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserViolationStats(PathMetadata metadata, PathInits inits) {
        this(UserViolationStats.class, metadata, inits);
    }

    public QUserViolationStats(Class<? extends UserViolationStats> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user"), inits.get("user")) : null;
    }

}

