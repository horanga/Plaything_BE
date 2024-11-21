package com.plaything.api.domain.repository.entity.pay;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserRewardActivity is a Querydsl query type for UserRewardActivity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserRewardActivity extends EntityPathBase<UserRewardActivity> {

    private static final long serialVersionUID = -1237000533L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserRewardActivity userRewardActivity = new QUserRewardActivity("userRewardActivity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> lastAdViewTime = createDateTime("lastAdViewTime", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> lastLoginTime = createDate("lastLoginTime", java.time.LocalDate.class);

    public final com.plaything.api.domain.repository.entity.user.QUser user;

    public QUserRewardActivity(String variable) {
        this(UserRewardActivity.class, forVariable(variable), INITS);
    }

    public QUserRewardActivity(Path<? extends UserRewardActivity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserRewardActivity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserRewardActivity(PathMetadata metadata, PathInits inits) {
        this(UserRewardActivity.class, metadata, inits);
    }

    public QUserRewardActivity(Class<? extends UserRewardActivity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new com.plaything.api.domain.repository.entity.user.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

