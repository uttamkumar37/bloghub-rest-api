package com.bloghub.api.repository;

import com.bloghub.api.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Post entity with pagination, search, and author-scoped queries.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /** Get paginated posts by author */
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    /** Case-insensitive full-text search across title, description, and content */
    @Query("SELECT p FROM Post p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Post> searchPosts(@Param("query") String query, Pageable pageable);

    /** Filter by category */
    Page<Post> findByCategoryIgnoreCase(String category, Pageable pageable);
}
