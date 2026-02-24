package com.bloghub.repository;

import com.bloghub.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlugIgnoreCase(String slug);
    boolean existsByNameIgnoreCase(String name);
    boolean existsBySlugIgnoreCase(String slug);
}

