package com.thegamersstation.marketplace.city;

import com.thegamersstation.marketplace.city.dto.CityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityService {
    
    private final CityRepository cityRepository;
    private final CityMapper cityMapper;
    
    /**
     * Get all cities
     * @return List of all cities
     */
    public List<CityDto> getAllCities() {
        List<City> cities = cityRepository.findAll();
        return cityMapper.toDtoList(cities);
    }
    
    /**
     * Get cities by region ID
     * @param regionId Region ID
     * @return List of cities in the region
     */
    public List<CityDto> getCitiesByRegion(Long regionId) {
        List<City> cities = cityRepository.findByRegionId(regionId);
        return cityMapper.toDtoList(cities);
    }
}
