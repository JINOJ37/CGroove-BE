package com.example.cgroove.repository;

import com.example.cgroove.entity.Event;
import com.example.cgroove.entity.EventLike;
import com.example.cgroove.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventLikeRepository extends JpaRepository<EventLike, Long> {
    Optional<EventLike> findByEventAndUser(Event event, User user);
    boolean existsByEventEventIdAndUserUserId(Long eventId, Long userId);;
    @Query("SELECT el.event.eventId FROM EventLike el " +
            "WHERE el.user.userId = :userId AND el.event.eventId IN :eventIds")
    Set<Long> findLikedEventIds(@Param("eventIds") List<Long> eventIds, @Param("userId") Long userId);
}
