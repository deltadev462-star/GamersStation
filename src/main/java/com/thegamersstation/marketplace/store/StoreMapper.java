package com.thegamersstation.marketplace.store;

import com.thegamersstation.marketplace.store.dto.StoreDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StoreMapper {
    
    @Mapping(source = "owner.id", target = "ownerId")
    StoreDto toDto(Store store);
    
    List<StoreDto> toDtoList(List<Store> stores);
}
