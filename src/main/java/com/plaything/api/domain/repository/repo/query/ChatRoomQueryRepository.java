package com.plaything.api.domain.repository.repo.query;

import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.plaything.api.domain.repository.entity.chat.QChatRoom.chatRoom;
import static com.plaything.api.domain.repository.entity.user.profile.QProfile.profile;

@RequiredArgsConstructor
@Repository
public class ChatRoomQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<ChatRoom> findChatRooms(String requestNickname, Long lastChatRoomId) {
        BooleanBuilder whereCondition = getBooleanBuilder(requestNickname, lastChatRoomId);

        return jpaQueryFactory.selectFrom(chatRoom)
                .leftJoin(profile).on(profile.nickName.eq(chatRoom.senderNickname))
                .leftJoin(profile).on(profile.nickName.eq(chatRoom.receiverNickname))
                .where(whereCondition)
                .orderBy(chatRoom.id.desc())
                .limit(10)
                .fetch();
    }

    private BooleanBuilder getBooleanBuilder(String requestNickname, Long lastChatRoomId) {
        BooleanBuilder whereCondition = new BooleanBuilder();

        whereCondition.and(chatRoom.exitedUserNickname.isNull().or(
                chatRoom.exitedUserNickname.ne(requestNickname)
        ));

        whereCondition
                .and(chatRoom.receiverNickname.eq(requestNickname)
                        .or(chatRoom.senderNickname.eq(requestNickname))
                        .and(chatRoom.isClosed.isFalse())
                        .and(profile.isBaned.isFalse())
                        .and(profile.isDeleted.isFalse())
                );

        if (lastChatRoomId != null) {
            whereCondition.and(chatRoom.id.lt(lastChatRoomId));
        }
        return whereCondition;
    }
}
