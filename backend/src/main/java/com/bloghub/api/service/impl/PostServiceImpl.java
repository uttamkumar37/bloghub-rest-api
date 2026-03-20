package com.bloghub.api.service.impl;

import com.bloghub.api.dto.PagedResponse;
import com.bloghub.api.dto.PostDto;
import com.bloghub.api.dto.PostRequest;
import com.bloghub.api.entity.Post;
import com.bloghub.api.entity.Role;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.exception.ResourceNotFoundException;
import com.bloghub.api.repository.PostRepository;
import com.bloghub.api.repository.UserRepository;
import com.bloghub.api.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Post service implementation.
 * Handles CRUD, pagination/sorting, search, category filtering, and likes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PostDto createPost(PostRequest request, String username) {
        User author = findUserByUsername(username);

        Post post = Post.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .content(request.getContent())
                .category(request.getCategory())
                .author(author)
                .build();

        Post saved = postRepository.save(post);
        log.info("Post '{}' created by user '{}'", saved.getId(), username);
        return mapToDto(saved, username);
    }

    @Override
    @Transactional
    public PostDto updatePost(Long postId, PostRequest request, String username) {
        Post post = findPostById(postId);
        User requester = findUserByUsername(username);

        // Only the author or an admin can update the post
        if (!post.getAuthor().getId().equals(requester.getId()) && !isAdmin(requester)) {
            throw new BlogApiException(HttpStatus.FORBIDDEN, "You do not have permission to update this post");
        }

        if (StringUtils.hasText(request.getTitle())) post.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getDescription())) post.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getContent())) post.setContent(request.getContent());
        if (StringUtils.hasText(request.getCategory())) post.setCategory(request.getCategory());

        Post updated = postRepository.save(post);
        log.info("Post {} updated by '{}'", postId, username);
        return mapToDto(updated, username);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = findPostById(postId);
        User requester = findUserByUsername(username);

        // Admin can delete any post; users only delete their own
        if (!post.getAuthor().getId().equals(requester.getId()) && !isAdmin(requester)) {
            throw new BlogApiException(HttpStatus.FORBIDDEN, "You do not have permission to delete this post");
        }

        postRepository.delete(post);
        log.info("Post {} deleted by '{}'", postId, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PostDto getPostById(Long postId, String username) {
        Post post = findPostById(postId);
        return mapToDto(post, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PostDto> getAllPosts(int page, int size, String sortBy, String sortDir, String username) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> posts = postRepository.findAll(pageable);
        return buildPagedResponse(posts, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PostDto> getPostsByUser(Long userId, int page, int size, String username) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByAuthorId(userId, pageable);
        return buildPagedResponse(posts, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PostDto> searchPosts(String query, int page, int size, String username) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.searchPosts(query, pageable);
        return buildPagedResponse(posts, username);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PostDto> getPostsByCategory(String category, int page, int size, String username) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByCategoryIgnoreCase(category, pageable);
        return buildPagedResponse(posts, username);
    }

    /**
     * Toggles a like on a post.
     * If the user has already liked the post, the like is removed.
     * Otherwise, a new like is added.
     */
    @Override
    @Transactional
    public PostDto toggleLike(Long postId, String username) {
        Post post = findPostById(postId);
        User user = findUserByUsername(username);

        boolean alreadyLiked = post.getLikes().stream()
                .anyMatch(u -> u.getId().equals(user.getId()));

        if (alreadyLiked) {
            post.getLikes().removeIf(u -> u.getId().equals(user.getId()));
            log.info("User '{}' unliked post {}", username, postId);
        } else {
            post.getLikes().add(user);
            log.info("User '{}' liked post {}", username, postId);
        }

        Post updated = postRepository.save(post);
        return mapToDto(updated, username);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
    }

    private User findUserByUsername(String username) {
        if (!StringUtils.hasText(username)) return null;
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
    }

    private PagedResponse<PostDto> buildPagedResponse(Page<Post> postPage, String username) {
        List<PostDto> content = postPage.getContent().stream()
                .map(post -> mapToDto(post, username))
                .collect(Collectors.toList());

        return PagedResponse.<PostDto>builder()
                .content(content)
                .page(postPage.getNumber())
                .size(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .last(postPage.isLast())
                .first(postPage.isFirst())
                .build();
    }

    private PostDto mapToDto(Post post, String currentUsername) {
        boolean likedByCurrentUser = false;
        if (StringUtils.hasText(currentUsername)) {
            likedByCurrentUser = post.getLikes().stream()
                    .anyMatch(u -> u.getUsername().equals(currentUsername));
        }

        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .content(post.getContent())
                .category(post.getCategory())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getName())
                .authorUsername(post.getAuthor().getUsername())
                .commentsCount(post.getComments().size())
                .likesCount(post.getLikesCount())
                .likedByCurrentUser(likedByCurrentUser)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
