package com.thegamersstation.marketplace.store;

import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import com.thegamersstation.marketplace.common.util.SlugUtil;
import com.thegamersstation.marketplace.store.dto.CreateStoreRequest;
import com.thegamersstation.marketplace.store.dto.StoreDto;
import com.thegamersstation.marketplace.store.dto.UpdateStoreRequest;
import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StoreService {
    
    private final StoreRepository storeRepository;
    private final UsersRepository usersRepository;
    private final StoreMapper storeMapper;
    
    /**
     * Create a new store for a store manager
     * Only called by admin when creating a store manager
     */
    @Transactional
    public StoreDto createStore(Long ownerId, CreateStoreRequest request) {
        User owner = usersRepository.findById(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify user is a store manager
        if (owner.getRole() != User.UserRole.STORE_MANAGER) {
            throw new BusinessRuleException("User must be a STORE_MANAGER to own a store");
        }
        
        // Check if user already has a store
        if (storeRepository.existsByOwnerId(ownerId)) {
            throw new BusinessRuleException("User already owns a store");
        }
        
        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = SlugUtil.toSlug(request.getNameEn());
        }
        
        // Validate slug uniqueness
        if (storeRepository.existsBySlug(slug)) {
            throw new BusinessRuleException("Slug already exists");
        }
        
        Store store = Store.builder()
            .owner(owner)
            .nameEn(request.getNameEn())
            .nameAr(request.getNameAr())
            .slug(slug)
            .descriptionEn(request.getDescriptionEn())
            .descriptionAr(request.getDescriptionAr())
            .logoUrl(request.getLogoUrl())
            .bannerUrl(request.getBannerUrl())
            .isVerified(false)
            .isActive(true)
            .build();
        
        Store saved = storeRepository.save(store);
        log.info("Created store: {} for owner: {}", saved.getId(), ownerId);
        
        return storeMapper.toDto(saved);
    }
    
    /**
     * Update store information
     * Only the store owner can update their store
     */
    @Transactional
    public StoreDto updateStore(Long storeId, UpdateStoreRequest request, Long currentUserId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        
        // Verify ownership
        if (!store.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own store");
        }
        
        if (request.getNameEn() != null) {
            store.setNameEn(request.getNameEn());
        }
        
        if (request.getNameAr() != null) {
            store.setNameAr(request.getNameAr());
        }
        
        if (request.getDescriptionEn() != null) {
            store.setDescriptionEn(request.getDescriptionEn());
        }
        
        if (request.getDescriptionAr() != null) {
            store.setDescriptionAr(request.getDescriptionAr());
        }
        
        if (request.getLogoUrl() != null) {
            store.setLogoUrl(request.getLogoUrl());
        }
        
        if (request.getBannerUrl() != null) {
            store.setBannerUrl(request.getBannerUrl());
        }
        
        Store updated = storeRepository.save(store);
        log.info("Updated store: {}", storeId);
        
        return storeMapper.toDto(updated);
    }
    
    /**
     * Get store by ID
     */
    public StoreDto getStoreById(Long id) {
        Store store = storeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        
        if (!store.getIsActive()) {
            throw new ResourceNotFoundException("Store not found");
        }
        
        return storeMapper.toDto(store);
    }
    
    /**
     * Get store by slug
     */
    public StoreDto getStoreBySlug(String slug) {
        Store store = storeRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        
        if (!store.getIsActive()) {
            throw new ResourceNotFoundException("Store not found");
        }
        
        return storeMapper.toDto(store);
    }
    
    /**
     * Get store by owner ID
     */
    public StoreDto getStoreByOwnerId(Long ownerId) {
        Store store = storeRepository.findByOwnerId(ownerId)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        
        return storeMapper.toDto(store);
    }
    
    /**
     * List all active stores
     */
    public List<StoreDto> getAllStores() {
        List<Store> stores = storeRepository.findAll();
        return storeMapper.toDtoList(stores.stream()
            .filter(Store::getIsActive)
            .toList());
    }
    
    /**
     * Verify/unverify store (admin only)
     */
    @Transactional
    public StoreDto setStoreVerification(Long storeId, boolean verified) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        
        store.setIsVerified(verified);
        Store updated = storeRepository.save(store);
        log.info("{} store: {}", verified ? "Verified" : "Unverified", storeId);
        
        return storeMapper.toDto(updated);
    }
    
    /**
     * Deactivate store (admin only)
     */
    @Transactional
    public void deactivateStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        
        store.setIsActive(false);
        storeRepository.save(store);
        log.info("Deactivated store: {}", storeId);
    }
}
