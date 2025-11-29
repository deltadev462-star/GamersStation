package com.thegamersstation.marketplace.city;

import com.thegamersstation.marketplace.city.dto.CityDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CityMapper {
    
    CityDto toDto(City city);
    
    List<CityDto> toDtoList(List<City> cities);
}
