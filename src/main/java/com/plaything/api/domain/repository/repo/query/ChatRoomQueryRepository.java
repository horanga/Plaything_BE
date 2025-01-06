package com.plaything.api.domain.repository.repo.query;

import com.plaything.api.domain.repository.entity.chat.ChatRoom;
import com.plaything.api.domain.repository.entity.profile.QProfile;
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

    public List<ChatRoom> findChatRooms(String requestLoginId, Long lastChatRoomId) {
        QProfile senderProfile = new QProfile("senderProfile");
        QProfile receiverProfile = new QProfile("receiverProfile");

        BooleanBuilder whereCondition = getBooleanBuilder(requestLoginId, lastChatRoomId);

        return jpaQueryFactory.selectFrom(chatRoom)
                .innerJoin(senderProfile).on(senderProfile.user.loginId.eq(chatRoom.senderLoginId))
                .innerJoin(receiverProfile).on(receiverProfile.user.loginId.eq(chatRoom.receiverLoginId))
                .where(whereCondition)
                .where(
                        chatRoom.receiverLoginId.eq(requestLoginId)
                                .and(senderProfile.isBaned.isFalse())
                                .and(senderProfile.isDeleted.isFalse())
                                .or(
                                        chatRoom.senderLoginId.eq(requestLoginId)
                                                .and(receiverProfile.isBaned.isFalse())
                                                .and(receiverProfile.isDeleted.isFalse())
                                )
                )
                .where(chatRoom.isClosed.isFalse())
                .orderBy(chatRoom.id.desc())
                .limit(10)
                .fetch();
    }

    public boolean findNewChat(String lgoinId) {
        return jpaQueryFactory.selectFrom(chatRoom)
                .where(
                        chatRoom.hasNewChat.isTrue()
                                .and(
                                        chatRoom.receiverLoginId.eq(lgoinId)
                                                .or(chatRoom.senderLoginId.eq(lgoinId))
                                ).and(chatRoom.lastChatSender.ne(lgoinId))
                )
                .fetchOne() != null;
    }

    private BooleanBuilder getBooleanBuilder(String requestLoginId, Long lastChatRoomId) {
        BooleanBuilder whereCondition = new BooleanBuilder();

        whereCondition.and(chatRoom.exitedUserLoginId.isNull().or(
                chatRoom.exitedUserLoginId.ne(requestLoginId)
        ));

        if (lastChatRoomId != null) {
            whereCondition.and(chatRoom.id.lt(lastChatRoomId));
        }
        return whereCondition;
    }
}
