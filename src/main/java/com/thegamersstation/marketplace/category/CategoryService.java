package com.thegamersstation.marketplace.category;

import com.thegamersstation.marketplace.category.dto.CategoryTreeDto;
import com.thegamersstation.marketplace.category.dto.CreateCategoryDto;
import com.thegamersstation.marketplace.category.dto.ReorderCategoriesDto;
import com.thegamersstation.marketplace.category.dto.UpdateCategoryDto;
import com.thegamersstation.marketplace.category.mapper.CategoryMapper;
import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import com.thegamersstation.marketplace.common.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryTreeDto> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAllByOrderByParentIdAscSortOrderAsc();
        
        // Build tree structure
        Map<Long, Category> categoryMap = new HashMap<>();
        List<Category> rootCategories = new ArrayList<>();

        // First pass: create map
        for (Category category : allCategories) {
            categoryMap.put(category.getId(), category);
        }

        // Second pass: build tree
        for (Category category : allCategories) {
            if (category.getParentId() == null) {
                rootCategories.add(category);
            } else {
                Category parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(category);
                }
            }
        }

        // Convert to DTOs
        return rootCategories.stream()
                .map(this::buildTreeDto)
                .collect(Collectors.toList());
    }

    private CategoryTreeDto buildTreeDto(Category category) {
        CategoryTreeDto dto = categoryMapper.toTreeDto(category);
        if (!category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .map(this::buildTreeDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public CategoryTreeDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return categoryMapper.toTreeDto(category);
    }

    @Transactional
    public CategoryTreeDto createCategory(CreateCategoryDto createDto) {
        // Determine level based on parent
        int level = 1;
        if (createDto.getParentId() != null) {
            Category parent = categoryRepository.findById(createDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            level = parent.getLevel() + 1;
            
            if (level > 3) {
                throw new BusinessRuleException("Maximum category depth is 3 levels");
            }
        }

        // Generate slug if not provided
        String slug = createDto.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = SlugUtil.toSlug(createDto.getNameEn());
        }

        // Validate slug uniqueness
        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("Slug already exists");
        }

        // Determine sort order
        int sortOrder = createDto.getSortOrder() != null ? createDto.getSortOrder() : 0;

        Category category = Category.builder()
                .nameEn(createDto.getNameEn())
                .nameAr(createDto.getNameAr())
                .slug(slug)
                .parentId(createDto.getParentId())
                .level(level)
                .sortOrder(sortOrder)
                .isActive(true)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category: {} (level {})", saved.getId(), saved.getLevel());
        
        return categoryMapper.toTreeDto(saved);
    }

    @Transactional
    public CategoryTreeDto updateCategory(Long id, UpdateCategoryDto updateDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Update names
        if (updateDto.getNameEn() != null) {
            category.setNameEn(updateDto.getNameEn());
        }
        if (updateDto.getNameAr() != null) {
            category.setNameAr(updateDto.getNameAr());
        }

        // Update slug
        if (updateDto.getSlug() != null) {
            if (categoryRepository.existsBySlugAndIdNot(updateDto.getSlug(), id)) {
                throw new BusinessRuleException("Slug already exists");
            }
            category.setSlug(updateDto.getSlug());
        }

        // Update parent (validate level constraints)
        if (updateDto.getParentId() != null && !updateDto.getParentId().equals(category.getParentId())) {
            Category newParent = categoryRepository.findById(updateDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            
            int newLevel = newParent.getLevel() + 1;
            if (newLevel > 3) {
                throw new BusinessRuleException("Moving this category would exceed maximum depth of 3 levels");
            }
            
            // Check if new parent is not a child of current category
            if (isDescendant(id, updateDto.getParentId())) {
                throw new BusinessRuleException("Cannot move category to its own descendant");
            }
            
            category.setParentId(updateDto.getParentId());
            category.setLevel(newLevel);
        }

        // Update sort order
        if (updateDto.getSortOrder() != null) {
            category.setSortOrder(updateDto.getSortOrder());
        }

        // Update active status
        if (updateDto.getIsActive() != null) {
            category.setIsActive(updateDto.getIsActive());
        }

        Category saved = categoryRepository.save(category);
        log.info("Updated category: {}", saved.getId());
        
        return categoryMapper.toTreeDto(saved);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Check for children
        long childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new BusinessRuleException("Cannot delete category with children");
        }

        // TODO: Check for ads when Ads module is implemented
        // long adsCount = categoryRepository.countAdsByCategoryId(id);
        // if (adsCount > 0) {
        //     throw new BusinessRuleException("Cannot delete category with associated ads");
        // }

        categoryRepository.delete(category);
        log.info("Deleted category: {}", id);
    }

    @Transactional
    public void reorderCategories(ReorderCategoriesDto reorderDto) {
        List<Long> categoryIds = reorderDto.getCategoryIds();
        
        // Validate all categories exist and belong to same parent
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
            
            // Verify parent matches
            Long expectedParent = reorderDto.getParentId();
            if ((expectedParent == null && category.getParentId() != null) ||
                (expectedParent != null && !expectedParent.equals(category.getParentId()))) {
                throw new BusinessRuleException("Category " + categoryId + " does not belong to specified parent");
            }
            
            // Update sort order
            category.setSortOrder(i);
            categoryRepository.save(category);
        }
        
        log.info("Reordered {} categories under parent {}", categoryIds.size(), reorderDto.getParentId());
    }

    private boolean isDescendant(Long ancestorId, Long descendantId) {
        if (ancestorId.equals(descendantId)) {
            return true;
        }
        
        Category descendant = categoryRepository.findById(descendantId).orElse(null);
        if (descendant == null || descendant.getParentId() == null) {
            return false;
        }
        
        return isDescendant(ancestorId, descendant.getParentId());
    }
}
