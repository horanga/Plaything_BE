package com.plaything.api.domain.repository.repo.query;

import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.user.profile.QProfile;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.plaything.api.domain.repository.entity.chat.QChatRoom.chatRoom;

@RequiredArgsConstructor
@Repository
public class ChatRoomQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<ChatRoom> findChatRooms(String requestNickname, Long lastChatRoomId) {
        QProfile senderProfile = new QProfile("senderProfile");
        QProfile receiverProfile = new QProfile("receiverProfile");

        BooleanBuilder whereCondition = getBooleanBuilder(requestNickname, lastChatRoomId);

        return jpaQueryFactory.selectFrom(chatRoom)
                .leftJoin(senderProfile).on(senderProfile.nickName.eq(chatRoom.senderNickname))
                .leftJoin(receiverProfile).on(receiverProfile.nickName.eq(chatRoom.receiverNickname))
                .where(whereCondition)
                .where(
                        chatRoom.receiverNickname.eq(requestNickname)
                                .and(senderProfile.isBaned.isFalse())
                                .and(senderProfile.isDeleted.isFalse())
                                .or(
                                        chatRoom.senderNickname.eq(requestNickname)
                                                .and(receiverProfile.isBaned.isFalse())
                                                .and(receiverProfile.isDeleted.isFalse())
                                )
                )
                .where(chatRoom.isClosed.isFalse())
                .orderBy(chatRoom.id.desc())
                .limit(10)
                .fetch();
    }

    private BooleanBuilder getBooleanBuilder(String requestNickname, Long lastChatRoomId) {
        BooleanBuilder whereCondition = new BooleanBuilder();

        whereCondition.and(chatRoom.exitedUserNickname.isNull().or(
                chatRoom.exitedUserNickname.ne(requestNickname)
        ));

        if (lastChatRoomId != null) {
            whereCondition.and(chatRoom.id.lt(lastChatRoomId));
        }
        return whereCondition;
    }
}
