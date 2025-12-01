package com.example.dance_community.repository;

import com.example.dance_community.entity.Event;
import com.example.dance_community.entity.EventLike;
import com.example.dance_community.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventLikeRepository extends JpaRepository<EventLike, Long> {
    Optional<EventLike> findByEventAndUser(Event event, User user);
    boolean existsByEventEventIdAndUserUserId(Long eventId, Long userId);;
}
