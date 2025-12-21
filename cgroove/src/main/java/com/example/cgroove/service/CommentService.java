package com.example.cgroove.service;

import com.example.cgroove.dto.comment.CommentRequest;
import com.example.cgroove.dto.comment.CommentResponse;
import com.example.cgroove.entity.Comment;
import com.example.cgroove.entity.Event;
import com.example.cgroove.entity.Post;
import com.example.cgroove.entity.User;
import com.example.cgroove.exception.AccessDeniedException;
import com.example.cgroove.exception.InvalidRequestException;
import com.example.cgroove.exception.NotFoundException;
import com.example.cgroove.repository.CommentRepository;
import com.example.cgroove.repository.EventRepository;
import com.example.cgroove.repository.PostRepository;
import com.example.cgroove.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CommentResponse createComment(Long userId, CommentRequest request) {
        validateOneTargetOnly(request.getPostId(), request.getEventId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Post post = null;
        Event event = null;

        if (request.getPostId() != null) {
            post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
        } else {
            event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new NotFoundException("행사를 찾을 수 없습니다."));
        }

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .event(event)
                .content(request.getContent())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.from(savedComment, userId);
    }

    public List<CommentResponse> getComments(Long postId, Long eventId, Long currentUserId) {
        validateOneTargetOnly(postId, eventId);

        List<Comment> commentList;

        if (postId != null) {
            if (!postRepository.existsById(postId)) {
                throw new NotFoundException("게시글을 찾을 수 없습니다.");
            }
            commentList = commentRepository.findByPost_PostId(postId);
        } else {
            if (!eventRepository.existsById(eventId)) {
                throw new NotFoundException("행사를 찾을 수 없습니다.");
            }
            commentList = commentRepository.findByEvent_EventId(eventId);
        }

        return commentList.stream()
                .map(comment -> CommentResponse.from(comment, currentUserId))
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, String newContent) {
        Comment comment = findComment(commentId);
        validateOwner(comment, userId);

        comment.updateContent(newContent);
        return CommentResponse.from(comment, userId);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = findComment(commentId);
        validateOwner(comment, userId);

        comment.delete();
    }

    private void validateOneTargetOnly(Long postId, Long eventId) {
        if (postId == null && eventId == null) {
            throw new InvalidRequestException("댓글은 게시글 또는 행사에 작성해야 합니다.");
        }
        if (postId != null && eventId != null) {
            throw new InvalidRequestException("댓글은 게시글과 행사에 동시에 작성할 수 없습니다.");
        }
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
    }

    private void validateOwner(Comment comment, Long userId) {
        if (!comment.getUser().getUserId().equals(userId)) {
            throw new AccessDeniedException("작성자만 수정/삭제할 수 있습니다.");
        }
    }
}