package com.example.dance_community.repository;

import com.example.dance_community.entity.Event;
import com.example.dance_community.entity.EventJoin;
import com.example.dance_community.entity.User;
import com.example.dance_community.enums.EventJoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventJoinRepository extends JpaRepository<EventJoin, Long> {
    List<EventJoin> findByEvent(Event event);

    List<EventJoin> findByParticipant(User user);

    boolean existsByParticipantAndEvent(User user, Event event);

    void deleteByParticipantAndEvent(User user, Event event);

    Optional<EventJoin> findByParticipant_UserIdAndEvent_EventId(Long userId, Long eventId);

    long countByEvent_EventIdAndStatus(Long eventId, EventJoinStatus status);
}