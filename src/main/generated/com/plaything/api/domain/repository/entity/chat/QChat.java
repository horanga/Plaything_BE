package com.plaything.api.domain.repository.entity.chat;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChat is a Querydsl query type for Chat
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChat extends EntityPathBase<Chat> {

  public static final QChat chat = new QChat("chat");
  private static final long serialVersionUID = 1275475794L;
  private static final PathInits INITS = PathInits.DIRECT2;
  public final QChatRoom chatRoom;

  public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt",
      java.time.LocalDateTime.class);

  public final NumberPath<Long> id = createNumber("id", Long.class);

  public final StringPath message = createString("chat");

  public final StringPath receiver = createString("receiver");

  public final StringPath sender = createString("sender");

  public QChat(String variable) {
    this(Chat.class, forVariable(variable), INITS);
  }

  public QChat(Path<? extends Chat> path) {
    this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
  }

  public QChat(PathMetadata metadata) {
    this(metadata, PathInits.getFor(metadata, INITS));
  }

  public QChat(PathMetadata metadata, PathInits inits) {
    this(Chat.class, metadata, inits);
  }

  public QChat(Class<? extends Chat> type, PathMetadata metadata, PathInits inits) {
    super(type, metadata, inits);
    this.chatRoom = inits.isInitialized("chatRoom") ? new QChatRoom(forProperty("chatRoom")) : null;
  }

}

