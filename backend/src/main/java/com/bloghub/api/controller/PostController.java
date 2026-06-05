package com.bloghub.api.controller;

import com.bloghub.api.dto.ApiResponse;
import com.bloghub.api.dto.KeysetPageResponse;
import com.bloghub.api.dto.PagedResponse;
import com.bloghub.api.dto.PostDto;
import com.bloghub.api.dto.PostRequest;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.service.IdempotencyService;
import com.bloghub.api.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Blog post CRUD, search, pagination, and likes endpoints.
 *
 * - GET endpoints (list + single) are public
 * - Write endpoints require JWT authentication
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Blog post management APIs")
public class PostController {

    private final PostService postService;
    private final IdempotencyService idempotencyService;

    // ── Public read endpoints ───────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all posts with pagination and sorting")
    public ResponseEntity<PagedResponse<PostDto>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(postService.getAllPosts(page, size, sortBy, sortDir, username));
    }

    @GetMapping("/keyset")
    @Operation(summary = "Get recent posts using keyset pagination")
    public ResponseEntity<KeysetPageResponse<PostDto>> getRecentPostsByCursor(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(postService.getRecentPostsAfter(cursor, size, username));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "Get a specific post by ID")
    public ResponseEntity<PostDto> getPostById(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(postService.getPostById(postId, username));
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts by keyword (title, description, content)")
    public ResponseEntity<PagedResponse<PostDto>> searchPosts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(postService.searchPosts(query, page, size, username));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get posts filtered by category")
    public ResponseEntity<PagedResponse<PostDto>> getPostsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(postService.getPostsByCategory(category, page, size, username));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all posts by a specific user")
    public ResponseEntity<PagedResponse<PostDto>> getPostsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(postService.getPostsByUser(userId, page, size, username));
    }

    // ── Authenticated write endpoints ───────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new blog post", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PostDto> createPost(
            @Valid @RequestBody PostRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal UserDetails userDetails) {
        assertIdempotentCreateAllowed(idempotencyKey, userDetails.getUsername());
        PostDto created = postService.createPost(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{postId}")
    @Operation(summary = "Update an existing post (author or admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        PostDto updated = postService.updatePost(postId, request, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Delete a post (author or admin only)",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.deletePost(postId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully"));
    }

    @PostMapping("/{postId}/like")
    @Operation(summary = "Toggle like/unlike on a post",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<PostDto> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PostDto post = postService.toggleLike(postId, userDetails.getUsername());
        return ResponseEntity.ok(post);
    }

    private void assertIdempotentCreateAllowed(String idempotencyKey, String username) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }
        boolean claimed = idempotencyService.claim("post:create:" + username + ":" + idempotencyKey, Duration.ofHours(24));
        if (!claimed) {
            throw new BlogApiException(HttpStatus.CONFLICT, "Duplicate request detected for this Idempotency-Key");
        }
    }
}
