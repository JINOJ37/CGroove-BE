package com.example.dance_community.repository;

import com.example.dance_community.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.eventId = :eventId")
    Optional<Event> findWithLockByEventId(@Param("eventId") Long eventId);

    @Modifying()
    @Query("UPDATE EVENT p SET p.isDeleted = true WHERE p.host.userId = :userId")
    void softDeleteByUserId(@Param("userId") Long userId);

    @Modifying()
    @Query("UPDATE EVENT p SET p.isDeleted = true WHERE p.club.clubId = :clubId")
    void softDeleteByClubId(@Param("clubId") Long clubId);
}