package com.example.dance_community.service;

import com.example.dance_community.dto.like.EventlikeResponse;
import com.example.dance_community.entity.*;
import com.example.dance_community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventLikeService {
    private final EventRepository eventRepository;
    private final EventLikeRepository eventLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventlikeResponse toggleLike(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("행사를 찾을 수 없습니다"));

        Optional<EventLike> existingLike = eventLikeRepository.findByEventAndUser(event, user);

        boolean isLiked;

        if (existingLike.isPresent()) {
            eventLikeRepository.delete(existingLike.get());
            event.decrementLikeCount();
            isLiked = false;
        } else {
            EventLike newLike = EventLike.builder()
                    .event(event)
                    .user(user)
                    .build();
            eventLikeRepository.save(newLike);
            event.incrementLikeCount();
            isLiked = true;
        }

        return new EventlikeResponse(isLiked, event.getLikeCount());
    }
}

