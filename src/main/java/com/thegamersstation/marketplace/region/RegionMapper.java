package com.thegamersstation.marketplace.region;

import com.thegamersstation.marketplace.region.dto.RegionDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegionMapper {
    
    RegionDto toDto(Region region);
    
    List<RegionDto> toDtoList(List<Region> regions);
}
