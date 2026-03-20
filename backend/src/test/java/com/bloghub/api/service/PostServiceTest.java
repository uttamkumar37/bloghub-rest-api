package com.bloghub.api.service;

import com.bloghub.api.dto.PostDto;
import com.bloghub.api.dto.PostRequest;
import com.bloghub.api.entity.Post;
import com.bloghub.api.entity.Role;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.exception.ResourceNotFoundException;
import com.bloghub.api.repository.PostRepository;
import com.bloghub.api.repository.UserRepository;
import com.bloghub.api.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService unit tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private User author;
    private User admin;
    private Post post;
    private PostRequest postRequest;

    @BeforeEach
    void setUp() {
        Role userRole = Role.builder().id(1L).name(Role.RoleName.ROLE_USER).build();
        Role adminRole = Role.builder().id(2L).name(Role.RoleName.ROLE_ADMIN).build();

        author = User.builder()
                .id(1L)
                .name("Alice")
                .username("alice")
                .email("alice@example.com")
                .password("hashedPwd")
                .roles(new HashSet<>(Set.of(userRole)))
                .build();

        admin = User.builder()
                .id(2L)
                .name("Admin")
                .username("admin")
                .email("admin@example.com")
                .password("hashedPwd")
                .roles(new HashSet<>(Set.of(adminRole, userRole)))
                .build();

        post = Post.builder()
                .id(1L)
                .title("Test Post")
                .description("A test post description")
                .content("Content of the test post with enough characters")
                .category("Technology")
                .author(author)
                .build();

        postRequest = new PostRequest();
        postRequest.setTitle("Test Post");
        postRequest.setDescription("A test post description");
        postRequest.setContent("Content of the test post with enough characters");
        postRequest.setCategory("Technology");
    }

    @Nested
    @DisplayName("Create post")
    class CreatePost {

        @Test
        @DisplayName("should create post successfully")
        void createPost_success() {
            given(userRepository.findByUsername("alice")).willReturn(Optional.of(author));
            given(postRepository.save(any(Post.class))).willReturn(post);

            PostDto result = postService.createPost(postRequest, "alice");

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Post");
            assertThat(result.getAuthorUsername()).isEqualTo("alice");
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void createPost_userNotFound() {
            given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.createPost(postRequest, "unknown"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    @Nested
    @DisplayName("Delete post")
    class DeletePost {

        @Test
        @DisplayName("author can delete their own post")
        void deletePost_authorCanDelete() {
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(userRepository.findByUsername("alice")).willReturn(Optional.of(author));

            postService.deletePost(1L, "alice");

            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("admin can delete any post")
        void deletePost_adminCanDelete() {
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(userRepository.findByUsername("admin")).willReturn(Optional.of(admin));

            postService.deletePost(1L, "admin");

            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("non-author user cannot delete someone else's post")
        void deletePost_unauthorizedUser() {
            User other = User.builder()
                    .id(3L).username("bob")
                    .roles(new HashSet<>(Set.of(
                            Role.builder().id(1L).name(Role.RoleName.ROLE_USER).build())))
                    .build();

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(userRepository.findByUsername("bob")).willReturn(Optional.of(other));

            assertThatThrownBy(() -> postService.deletePost(1L, "bob"))
                    .isInstanceOf(BlogApiException.class)
                    .hasMessageContaining("permission");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for non-existent post")
        void deletePost_postNotFound() {
            given(postRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.deletePost(99L, "alice"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Post");
        }
    }

    @Nested
    @DisplayName("Toggle like")
    class ToggleLike {

        @Test
        @DisplayName("user can like a post they haven't liked yet")
        void toggleLike_addLike() {
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(userRepository.findByUsername("alice")).willReturn(Optional.of(author));
            given(postRepository.save(any(Post.class))).willReturn(post);

            PostDto result = postService.toggleLike(1L, "alice");

            assertThat(result).isNotNull();
            verify(postRepository).save(post);
        }
    }
}
