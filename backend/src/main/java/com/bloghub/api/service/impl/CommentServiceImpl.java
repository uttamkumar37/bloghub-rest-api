package com.bloghub.api.service.impl;

import com.bloghub.api.dto.CommentDto;
import com.bloghub.api.dto.CommentRequest;
import com.bloghub.api.entity.Comment;
import com.bloghub.api.entity.Post;
import com.bloghub.api.entity.Role;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.exception.ResourceNotFoundException;
import com.bloghub.api.repository.CommentRepository;
import com.bloghub.api.repository.PostRepository;
import com.bloghub.api.repository.UserRepository;
import com.bloghub.api.service.CommentService;
import com.bloghub.api.service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Comment service implementation.
 * Handles add, update, delete, and list operations for post comments.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public CommentDto addComment(Long postId, CommentRequest request, String username) {
        Post post = findPostById(postId);
        User author = findUserByUsername(username);

        Comment comment = Comment.builder()
                .body(request.getBody())
                .post(post)
                .author(author)
                .build();

        Comment saved = commentRepository.save(comment);
        outboxService.saveEvent(
                "COMMENT",
                String.valueOf(saved.getId()),
                "NEW_COMMENT_NOTIFICATION",
                "{\"postId\":" + postId + ",\"commentId\":" + saved.getId() + ",\"authorId\":" + author.getId() + "}"
        );
        log.info("Comment {} added to post {} by '{}'", saved.getId(), postId, username);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long postId, Long commentId, CommentRequest request, String username) {
        Comment comment = findCommentForPost(postId, commentId);
        User requester = findUserByUsername(username);

        // Only the comment author or an admin can update it
        if (!comment.getAuthor().getId().equals(requester.getId()) && !isAdmin(requester)) {
            throw new BlogApiException(HttpStatus.FORBIDDEN, "You do not have permission to update this comment");
        }

        comment.setBody(request.getBody());
        Comment updated = commentRepository.save(comment);
        log.info("Comment {} updated by '{}'", commentId, username);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, String username) {
        Comment comment = findCommentForPost(postId, commentId);
        User requester = findUserByUsername(username);

        // Admin can delete any comment; users only delete their own
        if (!comment.getAuthor().getId().equals(requester.getId()) && !isAdmin(requester)) {
            throw new BlogApiException(HttpStatus.FORBIDDEN, "You do not have permission to delete this comment");
        }

        commentRepository.delete(comment);
        log.info("Comment {} on post {} deleted by '{}'", commentId, postId, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByPost(Long postId) {
        // Ensure the post exists first
        findPostById(postId);
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    }

    private Comment findCommentForPost(Long postId, Long commentId) {
        // Validate post exists
        findPostById(postId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        // Ensure comment belongs to the specified post
        if (!comment.getPost().getId().equals(postId)) {
            throw new BlogApiException(HttpStatus.BAD_REQUEST,
                    "Comment with id " + commentId + " does not belong to post " + postId);
        }
        return comment;
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
    }

    private CommentDto mapToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .body(comment.getBody())
                .postId(comment.getPost().getId())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .authorUsername(comment.getAuthor().getUsername())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
