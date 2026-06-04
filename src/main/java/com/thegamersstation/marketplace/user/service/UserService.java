package com.thegamersstation.marketplace.user.service;

import com.thegamersstation.marketplace.common.exception.BusinessRuleException;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import com.thegamersstation.marketplace.city.CityRepository;
import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.repository.UsersRepository;
import com.thegamersstation.marketplace.user.dto.PublicUserProfileDto;
import com.thegamersstation.marketplace.user.dto.UpdateUserProfileDto;
import com.thegamersstation.marketplace.user.dto.UserProfileDto;
import com.thegamersstation.marketplace.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UsersRepository usersRepository;
    private final CityRepository cityRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUserProfile(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserProfileDto profileDto = userMapper.toProfileDto(user);
        
        // Add city information if cityId exists
        if (user.getCityId() != null) {
            cityRepository.findById(user.getCityId())
                    .ifPresent(city -> profileDto.setCity(
                        UserProfileDto.CityInfo.builder()
                            .id(city.getId())
                            .nameEn(city.getNameEn())
                            .nameAr(city.getNameAr())
                            .build()
                    ));
        }
        
        return profileDto;
    }

    @Transactional(readOnly = true)
    public PublicUserProfileDto getUserById(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.toPublicProfileDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateUserProfileDto updateDto) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Validate city exists if provided
        if (updateDto.getCityId() != null) {
            if (!cityRepository.existsById(updateDto.getCityId())) {
                throw new BusinessRuleException("Invalid city ID");
            }
            user.setCityId(updateDto.getCityId());
        }

        // Check username uniqueness if provided and different
        if (updateDto.getUsername() != null &&
            !updateDto.getUsername().equals(user.getUsername())) {
            if (usersRepository.existsByUsername(updateDto.getUsername())) {
                throw new BusinessRuleException("Username already taken","راحت عليك في واحد قبلك سبقك واخذ الاسم");
            }
            user.setUsername(updateDto.getUsername());
        }

        // Update fields
        if (updateDto.getEmail() != null) {
            user.setEmail(updateDto.getEmail());
        }

        // Update profile images
        if (updateDto.getProfileImage() != null) {
            user.setProfileImage(updateDto.getProfileImage());
        }
        if (updateDto.getBackgroundImage() != null) {
            user.setBackgroundImage(updateDto.getBackgroundImage());
        }

        // Update social media links
        if (updateDto.getFacebookLink() != null) {
            user.setFacebookLink(updateDto.getFacebookLink());
        }
        if (updateDto.getTwitterLink() != null) {
            user.setTwitterLink(updateDto.getTwitterLink());
        }
        if (updateDto.getInstagramLink() != null) {
            user.setInstagramLink(updateDto.getInstagramLink());
        }
        if (updateDto.getYoutubeLink() != null) {
            user.setYoutubeLink(updateDto.getYoutubeLink());
        }
        if (updateDto.getLinkedinLink() != null) {
            user.setLinkedinLink(updateDto.getLinkedinLink());
        }
        if (updateDto.getGithubLink() != null) {
            user.setGithubLink(updateDto.getGithubLink());
        }
        if (updateDto.getWebsiteLink() != null) {
            user.setWebsiteLink(updateDto.getWebsiteLink());
        }

        // Mark profile as completed if cityId is provided
        if (updateDto.getCityId() != null && !user.getProfileCompleted()) {
            user.setProfileCompleted(true);
            log.info("User {} profile completed", userId);
        }

        User savedUser = usersRepository.save(user);
        
        UserProfileDto profileDto = userMapper.toProfileDto(savedUser);
        
        // Add city information if cityId exists
        if (savedUser.getCityId() != null) {
            cityRepository.findById(savedUser.getCityId())
                    .ifPresent(city -> profileDto.setCity(
                        UserProfileDto.CityInfo.builder()
                            .id(city.getId())
                            .nameEn(city.getNameEn())
                            .nameAr(city.getNameAr())
                            .build()
                    ));
        }
        
        return profileDto;
    }
}
