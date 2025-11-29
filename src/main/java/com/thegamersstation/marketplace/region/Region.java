package com.thegamersstation.marketplace.region;

import com.thegamersstation.marketplace.city.City;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en", nullable = false, unique = true, length = 100)
    private String nameEn;

    @Column(name = "name_ar", nullable = false, unique = true, length = 100)
    private String nameAr;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @OneToMany(mappedBy = "region", fetch = FetchType.LAZY)
    @Builder.Default
    private List<City> cities = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
