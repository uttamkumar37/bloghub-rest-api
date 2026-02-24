package com.bloghub.service;

import com.bloghub.domain.Category;
import com.bloghub.domain.Post;
import com.bloghub.domain.RoleName;
import com.bloghub.domain.User;
import com.bloghub.exception.ForbiddenException;
import com.bloghub.exception.ResourceNotFoundException;
import com.bloghub.repository.CategoryRepository;
import com.bloghub.repository.CommentRepository;
import com.bloghub.repository.PostRepository;
import com.bloghub.repository.UserRepository;
import com.bloghub.security.BloghubUserPrincipal;
import com.bloghub.web.dto.PageResponse;
import com.bloghub.web.dto.post.PostCreateRequest;
import com.bloghub.web.dto.post.PostDto;
import com.bloghub.web.dto.post.PostUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository posts;
    private final CategoryRepository categories;
    private final UserRepository users;
    private final CommentRepository comments;

    public PostService(PostRepository posts, CategoryRepository categories, UserRepository users, CommentRepository comments) {
        this.posts = posts;
        this.categories = categories;
        this.users = users;
        this.comments = comments;
    }

    @Transactional(readOnly = true)
    public PageResponse<PostDto> list(Long categoryId, String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Post> result = posts.search(categoryId, keyword, pageable);
        Page<PostDto> mapped = result.map(this::toDtoWithCounts);
        return PageResponse.fromPage(mapped);
    }

    @Transactional(readOnly = true)
    public PostDto get(Long id) {
        Post post = posts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return toDtoWithCounts(post);
    }

    @Transactional
    public PostDto create(PostCreateRequest request, BloghubUserPrincipal currentUser) {
        Category category = categories.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        User author = users.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setCategory(category);
        post.setAuthor(author);

        Post saved = posts.save(post);
        return toDtoWithCounts(saved);
    }

    @Transactional
    public PostDto update(Long id, PostUpdateRequest request, BloghubUserPrincipal currentUser) {
        Post post = posts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        ensureCanModify(post, currentUser);

        Category category = categories.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setCategory(category);

        return toDtoWithCounts(post);
    }

    @Transactional
    public void delete(Long id, BloghubUserPrincipal currentUser) {
        Post post = posts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        ensureCanModify(post, currentUser);
        posts.delete(post);
    }

    private void ensureCanModify(Post post, BloghubUserPrincipal currentUser) {
        boolean isOwner = post.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == RoleName.ROLE_ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You are not allowed to modify this post");
        }
    }

    private PostDto toDtoWithCounts(Post post) {
        long commentCount = comments.countByPostId(post.getId());
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setImageUrl(post.getImageUrl());
        dto.setCategoryId(post.getCategory().getId());
        dto.setCategoryName(post.getCategory().getName());
        dto.setAuthorId(post.getAuthor().getId());
        dto.setAuthorName(post.getAuthor().getDisplayName());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setCommentsCount(commentCount);
        return dto;
    }
}

