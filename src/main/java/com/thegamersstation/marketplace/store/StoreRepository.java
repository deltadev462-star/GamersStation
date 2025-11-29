package com.thegamersstation.marketplace.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    Optional<Store> findBySlug(String slug);
    
    Optional<Store> findByOwnerId(Long ownerId);
    
    boolean existsBySlug(String slug);
    
    boolean existsByOwnerId(Long ownerId);
}
