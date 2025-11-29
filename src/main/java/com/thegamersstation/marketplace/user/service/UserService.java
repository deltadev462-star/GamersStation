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
        return userMapper.toProfileDto(user);
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

        // Validate city exists
        if (!cityRepository.existsById(updateDto.getCityId())) {
            throw new BusinessRuleException("Invalid city ID");
        }

        // Check username uniqueness if provided and different
        if (updateDto.getUsername() != null && 
            !updateDto.getUsername().equals(user.getUsername())) {
            if (usersRepository.existsByUsername(updateDto.getUsername())) {
                throw new BusinessRuleException("Username already taken");
            }
            user.setUsername(updateDto.getUsername());
        }

        // Update fields
        if (updateDto.getEmail() != null) {
            user.setEmail(updateDto.getEmail());
        }
        user.setCityId(updateDto.getCityId());

        // Mark profile as completed
        if (!user.getProfileCompleted()) {
            user.setProfileCompleted(true);
            log.info("User {} profile completed", userId);
        }

        User savedUser = usersRepository.save(user);
        return userMapper.toProfileDto(savedUser);
    }
}
