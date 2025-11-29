package com.thegamersstation.marketplace.admin.store;

import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import com.thegamersstation.marketplace.store.Store;
import com.thegamersstation.marketplace.store.StoreRepository;
import com.thegamersstation.marketplace.store.StoreService;
import com.thegamersstation.marketplace.store.dto.StoreDto;
import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreAdminService {
    
    private final UsersRepository usersRepository;
    private final StoreRepository storeRepository;
    private final StoreService storeService;
    
    /**
     * Create a store manager user with their store
     * Admin only operation
     */
    @Transactional
    public StoreManagerResponse createStoreManager(CreateStoreManagerRequest request) {
        // Check if phone number already exists
        if (usersRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new BusinessRuleException("Phone number already registered");
        }
        
        // Check if username already exists (if provided)
        if (request.getUsername() != null && usersRepository.existsByUsername(request.getUsername())) {
            throw new BusinessRuleException("Username already taken");
        }
        
        // Create user with STORE_MANAGER role
        User user = User.builder()
            .phoneNumber(request.getPhoneNumber())
            .username(request.getUsername())
            .email(request.getEmail())
            .cityId(request.getCityId())
            .role(User.UserRole.STORE_MANAGER)
            .isActive(true)
            .profileCompleted(request.getUsername() != null)
            .build();
        
        User savedUser = usersRepository.save(user);
        log.info("Created store manager user: {} ({})", savedUser.getId(), savedUser.getPhoneNumber());
        
        // Create store for the user
        StoreDto store = storeService.createStore(savedUser.getId(), request.getStore());
        log.info("Created store: {} for manager: {}", store.getId(), savedUser.getId());
        
        return StoreManagerResponse.builder()
            .userId(savedUser.getId())
            .phoneNumber(savedUser.getPhoneNumber())
            .username(savedUser.getUsername())
            .store(store)
            .build();
    }
    
    /**
     * Verify a store
     */
    @Transactional
    public StoreDto verifyStore(Long storeId) {
        return storeService.setStoreVerification(storeId, true);
    }
    
    /**
     * Unverify a store
     */
    @Transactional
    public StoreDto unverifyStore(Long storeId) {
        return storeService.setStoreVerification(storeId, false);
    }
    
    /**
     * Deactivate a store
     */
    @Transactional
    public void deactivateStore(Long storeId) {
        storeService.deactivateStore(storeId);
    }
    
    /**
     * Activate a store
     */
    @Transactional
    public void activateStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        
        store.setIsActive(true);
        storeRepository.save(store);
        log.info("Activated store: {}", storeId);
    }
}
