package com.plaything.api.domain.repository.repo.query;

import com.plaything.api.domain.repository.entity.chat.Chat;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.plaything.api.domain.repository.entity.chat.QChat.chat;

@RequiredArgsConstructor
@Repository
public class ChatQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<Chat> findChat(Long chatRoomId, Long lastChatId, LocalDate now) {

        LocalDateTime oneWeekAgo = now
                .atStartOfDay()  // 오늘 00:00:00
                .minusDays(7);   // 7일 전

        BooleanBuilder whereCondition = getBooleanBuilder(chatRoomId, lastChatId, oneWeekAgo);

        return jpaQueryFactory.selectFrom(chat)
                .where(whereCondition)
                .orderBy(chat.id.desc())
                .limit(10)
                .fetch();
    }

    private BooleanBuilder getBooleanBuilder(Long chatRoomId, Long lastChatId, LocalDateTime oneWeekAgo) {
        BooleanBuilder whereCondition = new BooleanBuilder();
        whereCondition.and(chat.chatRoom.id.eq(chatRoomId));
        whereCondition.and(chat.createdAt.after(oneWeekAgo));

        if (lastChatId != null) {
            whereCondition.and(chat.id.lt(lastChatId));
        }
        return whereCondition;
    }
}
