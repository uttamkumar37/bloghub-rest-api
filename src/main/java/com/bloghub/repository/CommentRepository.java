package com.bloghub.repository;

import com.bloghub.domain.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId, Sort sort);
    long countByPostId(Long postId);
}

