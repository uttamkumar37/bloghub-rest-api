package com.bloghub.api.repository;

import com.bloghub.api.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Comment entity queries.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** Retrieve all comments for a given post, ordered by creation date */
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

    /** Count comments for a post */
    long countByPostId(Long postId);
}
