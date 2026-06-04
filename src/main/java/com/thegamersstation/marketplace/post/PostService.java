package com.thegamersstation.marketplace.post;

import com.thegamersstation.marketplace.post.dto.PostDto;
import com.thegamersstation.marketplace.post.dto.CreatePostRequest;
import com.thegamersstation.marketplace.post.dto.UpdatePostRequest;
import com.thegamersstation.marketplace.category.Category;
import com.thegamersstation.marketplace.category.CategoryRepository;
import com.thegamersstation.marketplace.common.dto.PageResponseDto;
import com.thegamersstation.marketplace.city.City;
import com.thegamersstation.marketplace.city.CityRepository;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import com.thegamersstation.marketplace.common.util.ContentSanitizer;
import com.thegamersstation.marketplace.media.MediaService;
import com.thegamersstation.marketplace.security.SecurityUtil;
import com.thegamersstation.marketplace.store.Store;
import com.thegamersstation.marketplace.store.StoreRepository;
import com.thegamersstation.marketplace.survey.Question;
import com.thegamersstation.marketplace.survey.QuestionRepository;
import com.thegamersstation.marketplace.survey.SurveyResponse;
import com.thegamersstation.marketplace.survey.SurveyResponseRepository;
import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.thegamersstation.marketplace.common.exception.BusinessRuleException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    
    private final PostRepository postRepository;
    private final UsersRepository usersRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final StoreRepository storeRepository;
    private final PostMapper postMapper;
    private final ContentSanitizer contentSanitizer;
    private final MediaService mediaService;
    private final QuestionRepository questionRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    
    @Transactional
    public PostDto createPost(CreatePostRequest request, Long userId) {
        log.info("Creating post for user {} in category {}", userId, request.getCategoryId());
        
        User user = usersRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        
        if (!category.getIsActive()) {
            throw new BusinessRuleException(
                "Category is not active",
                "التصنيف غير مفعّل"
            );
        }
        
        City city = cityRepository.findById(request.getCityId())
            .orElseThrow(() -> new ResourceNotFoundException("City not found"));
        
        // Sanitize content
        String sanitizedTitle = contentSanitizer.sanitize(request.getTitle());
        String sanitizedDescription = contentSanitizer.maskPhoneNumbers(
            contentSanitizer.sanitize(request.getDescription())
        );
        
        // Link to store if user is a store manager
        Store store = null;
        if (user.getRole() == User.UserRole.STORE_MANAGER) {
            store = storeRepository.findByOwnerId(userId).orElse(null);
        }
        
        Post post = Post.builder()
            .owner(user)
            .store(store)
            .type(request.getType())
            .title(sanitizedTitle)
            .description(sanitizedDescription)
            .price(request.getPrice())
            .priceMin(request.getPriceMin())
            .priceMax(request.getPriceMax())
            .condition(request.getCondition())
            .category(category)
            .city(city)
            .status(Post.PostStatus.ACTIVE) // TODO: Change back to WAITING_APPROVAL when moderation is implemented
            .images(new ArrayList<>())
            .build();
        
        // Add images with derived thumbnail URLs
        for (int i = 0; i < request.getImageUrls().size(); i++) {
            String imageUrl = request.getImageUrls().get(i);
            PostImage image = PostImage.builder()
                .post(post)
                .url(imageUrl)
                .thumbnailUrl(mediaService.deriveThumbnailUrl(imageUrl))
                .sortOrder(i)
                .build();
            post.getImages().add(image);
        }
        
        Post savedPost = postRepository.save(post);
        return postMapper.toDto(savedPost);
    }
    
    @Transactional
    public PostDto updatePost(Long adId, UpdatePostRequest request, Long userId) {
        Post post = postRepository.findByIdAndNotDeleted(adId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (!post.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You can only update your own ads");
        }
        
        if (post.getStatus() == Post.PostStatus.SOLD || post.getStatus() == Post.PostStatus.BLOCKED) {
            throw new BusinessRuleException(
                "Cannot update post in current status",
                "لا يمكن تحديث الإعلان في حالته الحالية"
            );
        }
        
        if (request.getTitle() != null) {
            post.setTitle(contentSanitizer.sanitize(request.getTitle()));
        }
        
        if (request.getDescription() != null) {
            post.setDescription(contentSanitizer.maskPhoneNumbers(
                contentSanitizer.sanitize(request.getDescription())
            ));
        }
        
        if (request.getPrice() != null) {
            post.setPrice(request.getPrice());
        }
        
        if (request.getPriceMin() != null) {
            post.setPriceMin(request.getPriceMin());
        }
        
        if (request.getPriceMax() != null) {
            post.setPriceMax(request.getPriceMax());
        }
        
        if (request.getCondition() != null) {
            post.setCondition(request.getCondition());
        }
        
        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City not found"));
            post.setCity(city);
        }
        
        if (request.getImageUrls() != null) {
            // Remove all existing images (this will trigger cascade delete)
            List<PostImage> oldImages = new ArrayList<>(post.getImages());
            post.getImages().clear();
            
            // Flush to ensure old images are deleted before adding new ones
            postRepository.flush();
            
            // Add new images with derived thumbnail URLs
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                String imageUrl = request.getImageUrls().get(i);
                PostImage image = PostImage.builder()
                    .post(post)
                    .url(imageUrl)
                    .thumbnailUrl(mediaService.deriveThumbnailUrl(imageUrl))
                    .sortOrder(i)
                    .build();
                post.getImages().add(image);
            }
        }
        
        Post updatedPost = postRepository.save(post);
        return postMapper.toDto(updatedPost);
    }
    
    @Transactional(readOnly = true)
    public PostDto getPostById(Long adId) {
        Post post = postRepository.findByIdAndNotDeleted(adId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (post.getStatus() == Post.PostStatus.ACTIVE) {
            return postMapper.toDto(post);
        }
        
        // Allow owner and admin to view SOLD posts
        if (post.getStatus() == Post.PostStatus.SOLD) {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            boolean isOwner = currentUserId != null && currentUserId.equals(post.getOwner().getId());
            boolean isAdmin = SecurityUtil.isAdmin();
            if (isOwner || isAdmin) {
                return postMapper.toDto(post);
            }
        }
        
        throw new ResourceNotFoundException("Post not found");
    }
    
    @Transactional(readOnly = true)
    public PageResponseDto<PostDto> searchPosts(
        Long categoryId,
        String categoryIds,
        Long cityId,
        Post.PostType type,
        Post.PostCondition condition,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        Pageable pageable
    ) {
        // Parse categoryIds if provided
        List<Long> categoryIdList = null;
        if (categoryIds != null && !categoryIds.isEmpty()) {
            categoryIdList = Arrays.stream(categoryIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        }
        
        Page<Post> postsPage;
        if (categoryIdList != null) {
            postsPage = postRepository.searchPostsWithMultipleCategoriesAndPrice(categoryIdList, cityId, type, condition, minPrice, maxPrice, pageable);
        } else {
            postsPage = postRepository.searchPostsWithPrice(categoryId, cityId, type, condition, minPrice, maxPrice, pageable);
        }
        return PageResponseDto.of(postsPage.map(postMapper::toDto));
    }
    
    @Transactional(readOnly = true)
    public PageResponseDto<PostDto> advancedSearchPosts(
        String query,
        Long categoryId,
        Long cityId,
        Long regionId,
        Post.PostType type,
        Post.PostCondition condition,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        Pageable pageable
    ) {
        Page<Post> postsPage = postRepository.advancedSearchPosts(
            query, categoryId, cityId, regionId, type, condition, minPrice, maxPrice, pageable
        );
        return PageResponseDto.of(postsPage.map(postMapper::toDto));
    }
    
    @Transactional(readOnly = true)
    public PageResponseDto<PostDto> getMyPosts(Long userId, Pageable pageable) {
        Page<Post> postsPage = postRepository.findByOwnerIdAndNotDeleted(userId, pageable);
        return PageResponseDto.of(postsPage.map(postMapper::toDto));
    }
    
    @Transactional(readOnly = true)
    public PageResponseDto<PostDto> getPostsByStore(Long storeId, Pageable pageable) {
        Page<Post> postsPage = postRepository.findByStoreIdAndActive(storeId, pageable);
        return PageResponseDto.of(postsPage.map(postMapper::toDto));
    }
    
    @Transactional
    public void deletePost(Long adId, Long userId) {
        Post post = postRepository.findByIdAndNotDeleted(adId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (!post.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You can only delete your own ads");
        }
        
        post.setStatus(Post.PostStatus.DELETED);
        post.setDeletedAt(LocalDateTime.now());
        postRepository.save(post);
    }
    
    @Transactional
    public void markAsSold(Long adId, Long userId, boolean soldThroughPlatform) {
        Post post = postRepository.findByIdAndNotDeleted(adId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        
        if (!post.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("You can only mark your own ads as sold");
        }
        
        if (post.getStatus() != Post.PostStatus.ACTIVE) {
            throw new BusinessRuleException(
                "Only active ads can be marked as sold",
                "يمكن تحديد الإعلانات النشطة فقط كمباعة"
            );
        }
        
        post.setStatus(Post.PostStatus.SOLD);
        postRepository.save(post);
        
        // Save survey response for "Was the product sold through our platform?"
        Question question = questionRepository.findById(1L)
            .orElseThrow(() -> new ResourceNotFoundException("Survey question not found"));
        
        SurveyResponse response = SurveyResponse.builder()
            .post(post)
            .question(question)
            .responseValue(soldThroughPlatform ? "YES" : "NO")
            .build();
        surveyResponseRepository.save(response);
    }
}


