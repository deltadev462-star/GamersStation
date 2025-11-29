package com.thegamersstation.marketplace.post;

import com.thegamersstation.marketplace.post.dto.PostDto;
import com.thegamersstation.marketplace.post.dto.PostImageDto;
import com.thegamersstation.marketplace.common.util.LocalizationService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class PostMapper {
    
    @Autowired
    protected LocalizationService localizationService;
    
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerUsername", source = "owner.username")
    @Mapping(target = "store", expression = "java(mapStore(post))")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", expression = "java(getCategoryName(post))")
    @Mapping(target = "cityId", source = "city.id")
    @Mapping(target = "cityName", expression = "java(getCityName(post))")
    @Mapping(target = "images", expression = "java(mapImages(post.getImages()))")
    public abstract PostDto toDto(Post post);
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "thumbnailUrl", source = "thumbnailUrl")
    @Mapping(target = "sortOrder", source = "sortOrder")
    public abstract PostImageDto toImageDto(PostImage image);
    
    protected String getCategoryName(Post post) {
        if (post.getCategory() == null) return null;
        return localizationService.get(
            post.getCategory().getNameEn(),
            post.getCategory().getNameAr()
        );
    }
    
    protected String getCityName(Post post) {
        if (post.getCity() == null) return null;
        return localizationService.get(
            post.getCity().getNameEn(),
            post.getCity().getNameAr()
        );
    }
    
    protected List<PostImageDto> mapImages(List<PostImage> images) {
        if (images == null) return List.of();
        return images.stream()
            .map(this::toImageDto)
            .collect(Collectors.toList());
    }
    
    protected PostDto.StoreInfoDto mapStore(Post post) {
        if (post.getStore() == null) return null;
        return PostDto.StoreInfoDto.builder()
            .id(post.getStore().getId())
            .nameEn(post.getStore().getNameEn())
            .nameAr(post.getStore().getNameAr())
            .slug(post.getStore().getSlug())
            .logoUrl(post.getStore().getLogoUrl())
            .isVerified(post.getStore().getIsVerified())
            .build();
    }
}
