package com.thegamersstation.marketplace.store;

import com.thegamersstation.marketplace.common.dto.PageResponseDto;
import com.thegamersstation.marketplace.post.PostService;
import com.thegamersstation.marketplace.post.dto.PostDto;
import com.thegamersstation.marketplace.security.SecurityUtil;
import com.thegamersstation.marketplace.store.dto.StoreDto;
import com.thegamersstation.marketplace.store.dto.UpdateStoreRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Tag(name = "Stores", description = "Store management and browsing endpoints")
public class StoreController {
    
    private final StoreService storeService;
    private final PostService postService;
    
    @GetMapping
    @Operation(summary = "Get all stores", description = "Returns list of all active stores")
    public ResponseEntity<List<StoreDto>> getAllStores() {
        List<StoreDto> stores = storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get store by ID")
    public ResponseEntity<StoreDto> getStoreById(@PathVariable Long id) {
        StoreDto store = storeService.getStoreById(id);
        return ResponseEntity.ok(store);
    }
    
    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get store by slug")
    public ResponseEntity<StoreDto> getStoreBySlug(@PathVariable String slug) {
        StoreDto store = storeService.getStoreBySlug(slug);
        return ResponseEntity.ok(store);
    }
    
    @GetMapping("/{id}/posts")
    @Operation(summary = "Get all posts from a store")
    public ResponseEntity<PageResponseDto<PostDto>> getStorePosts(
        @PathVariable Long id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PageResponseDto<PostDto> posts = postService.getPostsByStore(id, pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/my-store")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my store", description = "Get the authenticated store manager's store")
    public ResponseEntity<StoreDto> getMyStore() {
        Long userId = SecurityUtil.getCurrentUserId();
        StoreDto store = storeService.getStoreByOwnerId(userId);
        return ResponseEntity.ok(store);
    }
    
    @PutMapping("/my-store")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update my store", description = "Update the authenticated store manager's store")
    public ResponseEntity<StoreDto> updateMyStore(@Valid @RequestBody UpdateStoreRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        // Get store ID from owner
        StoreDto currentStore = storeService.getStoreByOwnerId(userId);
        StoreDto updatedStore = storeService.updateStore(currentStore.getId(), request, userId);
        return ResponseEntity.ok(updatedStore);
    }
}
