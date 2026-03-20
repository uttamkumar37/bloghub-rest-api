package com.bloghub.api.service;

import com.bloghub.api.dto.PagedResponse;
import com.bloghub.api.dto.PostDto;
import com.bloghub.api.dto.PostRequest;

/**
 * Contract for blog post CRUD, pagination, search, and like operations.
 */
public interface PostService {

    PostDto createPost(PostRequest request, String username);

    PostDto updatePost(Long postId, PostRequest request, String username);

    void deletePost(Long postId, String username);

    PostDto getPostById(Long postId, String username);

    PagedResponse<PostDto> getAllPosts(int page, int size, String sortBy, String sortDir, String username);

    PagedResponse<PostDto> getPostsByUser(Long userId, int page, int size, String username);

    PagedResponse<PostDto> searchPosts(String query, int page, int size, String username);

    PagedResponse<PostDto> getPostsByCategory(String category, int page, int size, String username);

    PostDto toggleLike(Long postId, String username);
}
