package com.bloghub.web;

import com.bloghub.security.BloghubUserPrincipal;
import com.bloghub.security.CurrentUser;
import com.bloghub.service.PostService;
import com.bloghub.web.dto.ApiResponse;
import com.bloghub.web.dto.PageResponse;
import com.bloghub.web.dto.post.PostCreateRequest;
import com.bloghub.web.dto.post.PostDto;
import com.bloghub.web.dto.post.PostUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiPaths.V1 + "/posts")
@Validated
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostDto>>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        PageResponse<PostDto> posts = postService.list(categoryId, keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDto>> create(
            @Valid @RequestBody PostCreateRequest request,
            @CurrentUser BloghubUserPrincipal currentUser
    ) {
        PostDto dto = postService.create(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Post created", dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request,
            @CurrentUser BloghubUserPrincipal currentUser
    ) {
        PostDto dto = postService.update(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Post updated", dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @CurrentUser BloghubUserPrincipal currentUser
    ) {
        postService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}

