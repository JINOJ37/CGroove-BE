package com.example.cgroove.repository.impl;

import com.example.cgroove.entity.EventJoin;
import com.example.cgroove.enums.EventJoinStatus;
import com.example.cgroove.repository.custom.EventJoinRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.cgroove.entity.QEventJoin.eventJoin;
import static com.example.cgroove.entity.QUser.user;
import static com.example.cgroove.entity.QEvent.event;

@RequiredArgsConstructor
public class EventJoinRepositoryImpl implements EventJoinRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EventJoin> findParticipantsWithUser(Long eventId, EventJoinStatus status) {
        return queryFactory
                .selectFrom(eventJoin)
                .join(eventJoin.participant, user).fetchJoin()
                .where(
                        eventJoin.event.eventId.eq(eventId),
                        eventJoin.status.eq(status)
                )
                .orderBy(eventJoin.createdAt.asc())
                .fetch();
    }

    @Override
    public List<EventJoin> findMyJoinedEvents(Long userId, EventJoinStatus status) {
        return queryFactory
                .selectFrom(eventJoin)
                .join(eventJoin.event, event).fetchJoin()
                .join(event.host, user).fetchJoin()
                .where(
                        eventJoin.participant.userId.eq(userId),
                        eventJoin.status.eq(status)
                )
                .orderBy(eventJoin.createdAt.desc())
                .fetch();
    }
}