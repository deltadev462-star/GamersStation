package com.thegamersstation.marketplace.admin.store;

import com.thegamersstation.marketplace.store.dto.CreateStoreRequest;
import com.thegamersstation.marketplace.store.dto.StoreDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin - Stores", description = "Store management endpoints for administrators")
public class StoreAdminController {
    
    private final StoreAdminService storeAdminService;
    
    @PostMapping("/create-manager")
    @Operation(
        summary = "Create store manager with store",
        description = "Creates a new user with STORE_MANAGER role and their store in one request"
    )
    public ResponseEntity<StoreManagerResponse> createStoreManager(
        @Valid @RequestBody CreateStoreManagerRequest request
    ) {
        StoreManagerResponse response = storeAdminService.createStoreManager(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/{storeId}/verify")
    @Operation(summary = "Verify a store", description = "Mark a store as verified")
    public ResponseEntity<StoreDto> verifyStore(@PathVariable Long storeId) {
        StoreDto store = storeAdminService.verifyStore(storeId);
        return ResponseEntity.ok(store);
    }
    
    @PostMapping("/{storeId}/unverify")
    @Operation(summary = "Unverify a store", description = "Remove verification from a store")
    public ResponseEntity<StoreDto> unverifyStore(@PathVariable Long storeId) {
        StoreDto store = storeAdminService.unverifyStore(storeId);
        return ResponseEntity.ok(store);
    }
    
    @PostMapping("/{storeId}/deactivate")
    @Operation(summary = "Deactivate a store", description = "Deactivate a store (soft delete)")
    public ResponseEntity<Void> deactivateStore(@PathVariable Long storeId) {
        storeAdminService.deactivateStore(storeId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{storeId}/activate")
    @Operation(summary = "Activate a store", description = "Reactivate a deactivated store")
    public ResponseEntity<Void> activateStore(@PathVariable Long storeId) {
        storeAdminService.activateStore(storeId);
        return ResponseEntity.noContent().build();
    }
}
