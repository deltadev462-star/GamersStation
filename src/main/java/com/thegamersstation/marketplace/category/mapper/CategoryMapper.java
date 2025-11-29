package com.thegamersstation.marketplace.category.mapper;

import com.thegamersstation.marketplace.category.dto.CategoryTreeDto;
import com.thegamersstation.marketplace.category.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    CategoryTreeDto toTreeDto(Category category);
}
