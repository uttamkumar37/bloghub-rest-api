package com.bloghub.repository;

import com.bloghub.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("""
            select p from Post p
            where (:categoryId is null or p.category.id = :categoryId)
              and (
                :keyword is null
                or lower(p.title) like lower(concat('%', :keyword, '%'))
                or lower(p.content) like lower(concat('%', :keyword, '%'))
              )
            """)
    Page<Post> search(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);
}

