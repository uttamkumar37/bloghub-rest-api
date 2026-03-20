package com.bloghub.api.controller;

import com.bloghub.api.dto.PagedResponse;
import com.bloghub.api.dto.PostDto;
import com.bloghub.api.dto.PostRequest;
import com.bloghub.api.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@ActiveProfiles("test")
@DisplayName("PostController integration tests")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    private PostDto samplePost;

    @BeforeEach
    void setUp() {
        samplePost = PostDto.builder()
                .id(1L)
                .title("Test Post")
                .description("Test description")
                .content("Test content with enough characters here")
                .category("Technology")
                .authorId(1L)
                .authorName("Alice")
                .authorUsername("alice")
                .likesCount(0)
                .commentsCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("GET /posts - should return paginated posts without authentication")
    void getAllPosts_public() throws Exception {
        PagedResponse<PostDto> pagedResponse = PagedResponse.<PostDto>builder()
                .content(List.of(samplePost))
                .page(0).size(10).totalElements(1).totalPages(1).first(true).last(true)
                .build();

        given(postService.getAllPosts(anyInt(), anyInt(), anyString(), anyString(), any()))
                .willReturn(pagedResponse);

        mockMvc.perform(get("/posts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Post"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /posts/{id} - should return single post")
    void getPostById() throws Exception {
        given(postService.getPostById(eq(1L), any())).willReturn(samplePost);

        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    @DisplayName("POST /posts - authenticated user can create post")
    void createPost_authenticated() throws Exception {
        PostRequest request = new PostRequest();
        request.setTitle("New Post Title");
        request.setDescription("New post description text");
        request.setContent("This is the full content of the new post with enough characters");
        request.setCategory("Tech");

        given(postService.createPost(any(PostRequest.class), eq("alice"))).willReturn(samplePost);

        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    @DisplayName("POST /posts - unauthenticated request should return 401")
    void createPost_unauthenticated() throws Exception {
        PostRequest request = new PostRequest();
        request.setTitle("Unauthorized");
        request.setDescription("Should fail");
        request.setContent("Should fail as not authenticated");

        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice", roles = {"USER"})
    @DisplayName("POST /posts - missing title should return 400")
    void createPost_validationFailure() throws Exception {
        PostRequest request = new PostRequest();
        // Missing required title
        request.setDescription("Description");
        request.setContent("Content with enough characters for validation");

        mockMvc.perform(post("/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
