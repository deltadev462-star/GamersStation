package com.thegamersstation.marketplace.admin.user;

import com.thegamersstation.marketplace.common.dto.PageRequestDto;
import com.thegamersstation.marketplace.common.dto.PageResponseDto;
import com.thegamersstation.marketplace.common.exception.ResourceNotFoundException;
import com.thegamersstation.marketplace.user.repository.User;
import com.thegamersstation.marketplace.user.repository.UsersRepository;
import com.thegamersstation.marketplace.user.dto.UserProfileDto;
import com.thegamersstation.marketplace.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserModerationService {

    private final UsersRepository usersRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public PageResponseDto<UserProfileDto> getAllUsers(PageRequestDto pageRequest) {
        PageRequest springPageRequest = PageRequest.of(
                pageRequest.getPage(),
                pageRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> usersPage = usersRepository.findAll(springPageRequest);
        List<UserProfileDto> users = usersPage.getContent().stream()
                .map(userMapper::toProfileDto)
                .toList();

        return new PageResponseDto<>(
                users,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isFirst(),
                usersPage.isLast()
        );
    }

    @Transactional
    public UserProfileDto banUser(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(false);
        User savedUser = usersRepository.save(user);

        log.info("User {} has been banned", userId);
        return userMapper.toProfileDto(savedUser);
    }

    @Transactional
    public UserProfileDto unbanUser(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(true);
        User savedUser = usersRepository.save(user);

        log.info("User {} has been unbanned", userId);
        return userMapper.toProfileDto(savedUser);
    }
}
