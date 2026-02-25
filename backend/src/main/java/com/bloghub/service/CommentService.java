package com.bloghub.service;

import com.bloghub.domain.Comment;
import com.bloghub.domain.Post;
import com.bloghub.domain.RoleName;
import com.bloghub.domain.User;
import com.bloghub.exception.ForbiddenException;
import com.bloghub.exception.ResourceNotFoundException;
import com.bloghub.repository.CommentRepository;
import com.bloghub.repository.PostRepository;
import com.bloghub.repository.UserRepository;
import com.bloghub.security.BloghubUserPrincipal;
import com.bloghub.web.dto.comment.CommentCreateRequest;
import com.bloghub.web.dto.comment.CommentDto;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository comments;
    private final PostRepository posts;
    private final UserRepository users;

    public CommentService(CommentRepository comments, PostRepository posts, UserRepository users) {
        this.comments = comments;
        this.posts = posts;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listByPost(Long postId) {
        return comments.findByPostId(postId, Sort.by(Sort.Direction.ASC, "createdAt"))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CommentDto addComment(Long postId, CommentCreateRequest request, BloghubUserPrincipal currentUser) {
        Post post = posts.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = users.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setContent(request.getContent());

        Comment saved = comments.save(comment);
        return toDto(saved);
    }

    @Transactional
    public void deleteComment(Long commentId, BloghubUserPrincipal currentUser) {
        Comment comment = comments.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        boolean isOwner = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == RoleName.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You are not allowed to delete this comment");
        }
        comments.delete(comment);
    }

    private CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setAuthorName(comment.getAuthor().getDisplayName());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}

