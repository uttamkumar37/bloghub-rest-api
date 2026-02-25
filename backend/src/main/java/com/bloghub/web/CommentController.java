package com.bloghub.web;

import com.bloghub.security.BloghubUserPrincipal;
import com.bloghub.security.CurrentUser;
import com.bloghub.service.CommentService;
import com.bloghub.web.dto.ApiResponse;
import com.bloghub.web.dto.comment.CommentCreateRequest;
import com.bloghub.web.dto.comment.CommentDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1)
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentDto>>> listByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.listByPost(postId)));
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentDto>> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @CurrentUser BloghubUserPrincipal currentUser
    ) {
        CommentDto dto = commentService.addComment(postId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Comment added", dto));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long commentId,
            @CurrentUser BloghubUserPrincipal currentUser
    ) {
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.noContent().build();
    }
}

