package com.thegamersstation.marketplace.user.mapper;

import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.dto.PublicUserProfileDto;
import com.thegamersstation.marketplace.user.dto.UserProfileDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    UserProfileDto toProfileDto(User user);

    PublicUserProfileDto toPublicProfileDto(User user);
}
