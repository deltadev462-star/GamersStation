package com.thegamersstation.marketplace.region;

import com.thegamersstation.marketplace.region.dto.RegionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {
    
    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;
    
    /**
     * Get all regions
     * @return List of all regions
     */
    public List<RegionDto> getAllRegions() {
        List<Region> regions = regionRepository.findAll();
        return regionMapper.toDtoList(regions);
    }
}
