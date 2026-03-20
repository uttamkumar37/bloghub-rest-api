package com.bloghub.api.service;

import com.bloghub.api.dto.CommentDto;
import com.bloghub.api.dto.CommentRequest;

import java.util.List;

/**
 * Contract for comment operations on posts.
 */
public interface CommentService {

    CommentDto addComment(Long postId, CommentRequest request, String username);

    CommentDto updateComment(Long postId, Long commentId, CommentRequest request, String username);

    void deleteComment(Long postId, Long commentId, String username);

    List<CommentDto> getCommentsByPost(Long postId);
}
