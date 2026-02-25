package com.bloghub.service;

import com.bloghub.domain.Category;
import com.bloghub.exception.BadRequestException;
import com.bloghub.exception.ResourceNotFoundException;
import com.bloghub.repository.CategoryRepository;
import com.bloghub.util.SlugUtils;
import com.bloghub.web.dto.category.CategoryCreateRequest;
import com.bloghub.web.dto.category.CategoryDto;
import com.bloghub.web.dto.category.CategoryUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> listAll() {
        return categories.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CategoryDto getById(Long id) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return toDto(category);
    }

    @Transactional
    public CategoryDto create(CategoryCreateRequest request) {
        String slug = SlugUtils.toSlug(request.getName());
        if (slug == null) {
            throw new BadRequestException("Invalid category name");
        }
        if (categories.existsByNameIgnoreCase(request.getName()) || categories.existsBySlugIgnoreCase(slug)) {
            throw new BadRequestException("Category with same name already exists");
        }
        Category category = new Category();
        category.setName(request.getName());
        category.setSlug(slug);
        Category saved = categories.save(category);
        return toDto(saved);
    }

    @Transactional
    public CategoryDto update(Long id, CategoryUpdateRequest request) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        String slug = SlugUtils.toSlug(request.getName());
        if (slug == null) {
            throw new BadRequestException("Invalid category name");
        }
        category.setName(request.getName());
        category.setSlug(slug);
        return toDto(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!categories.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categories.deleteById(id);
    }

    private CategoryDto toDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }
}

