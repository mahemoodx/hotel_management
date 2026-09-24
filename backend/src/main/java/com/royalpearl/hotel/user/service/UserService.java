package com.royalpearl.hotel.user.service;

import com.royalpearl.hotel.config.AdminAllowlistProperties;
import com.royalpearl.hotel.exception.BadRequestException;
import com.royalpearl.hotel.exception.ConflictException;
import com.royalpearl.hotel.exception.ResourceNotFoundException;
import com.royalpearl.hotel.user.dto.UpdateProfileRequest;
import com.royalpearl.hotel.user.dto.UserDto;
import com.royalpearl.hotel.user.entity.AppRole;
import com.royalpearl.hotel.user.entity.User;
import com.royalpearl.hotel.user.entity.UserRole;
import com.royalpearl.hotel.user.mapper.UserMapper;
import com.royalpearl.hotel.user.repository.UserRepository;
import com.royalpearl.hotel.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository          userRepository;
    private final UserRoleRepository      userRoleRepository;
    private final UserMapper              userMapper;
    private final AdminAllowlistProperties allowlist;

    // ---------------------------------------------------------------
    // Profile
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public UserDto getMe(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public UserDto updateMe(UUID userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (req.getFullName()  != null) user.setFullName(req.getFullName());
        if (req.getPhone()     != null) user.setPhone(req.getPhone());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());

        return userMapper.toDto(userRepository.save(user));
    }

    // ---------------------------------------------------------------
    // Claim admin (allowlist-gated)
    // ---------------------------------------------------------------

    @Transactional
    public UserDto claimAdmin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean allowed = allowlist.getAllowlist().stream()
                .anyMatch(email -> email.equalsIgnoreCase(user.getEmail()));

        if (!allowed) {
            throw new BadRequestException("Your email is not on the admin allowlist");
        }

        if (userRoleRepository.existsByUserIdAndRole(userId, AppRole.admin)) {
            throw new ConflictException("User already has admin role");
        }

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(AppRole.admin)
                .build());

        log.warn("Admin role claimed by {}", user.getEmail());

        // Re-fetch to get updated roles
        return userMapper.toDto(userRepository.findById(userId).orElseThrow());
    }

    // ---------------------------------------------------------------
    // Admin user management
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<UserDto> adminListUsers(String search, Pageable pageable) {
        String q = (search == null || search.isBlank()) ? null : search;
        return userRepository.searchUsers(q, pageable).map(userMapper::toDto);
    }

    @Transactional
    public UserDto addRole(UUID userId, AppRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (userRoleRepository.existsByUserIdAndRole(userId, role)) {
            throw new ConflictException("User already has role: " + role.name());
        }

        userRoleRepository.save(UserRole.builder().user(user).role(role).build());
        log.info("Role {} added to user {}", role, userId);
        return userMapper.toDto(userRepository.findById(userId).orElseThrow());
    }

    @Transactional
    public UserDto removeRole(UUID userId, AppRole role) {
        userRoleRepository.findByUserIdAndRole(userId, role)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User " + userId + " does not have role: " + role.name()));

        userRoleRepository.findByUserIdAndRole(userId, role)
                .ifPresent(userRoleRepository::delete);

        log.info("Role {} removed from user {}", role, userId);
        return userMapper.toDto(userRepository.findById(userId).orElseThrow());
    }
}
