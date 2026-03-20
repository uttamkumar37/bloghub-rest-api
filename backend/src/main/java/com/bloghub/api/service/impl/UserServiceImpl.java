package com.bloghub.api.service.impl;

import com.bloghub.api.dto.UpdateUserRequest;
import com.bloghub.api.dto.UserDto;
import com.bloghub.api.entity.User;
import com.bloghub.api.exception.BlogApiException;
import com.bloghub.api.exception.ResourceNotFoundException;
import com.bloghub.api.repository.UserRepository;
import com.bloghub.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * User profile management service implementation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToDto(user);
    }

    /**
     * Updates user profile. Only the account owner can update their own data.
     */
    @Override
    @Transactional
    public UserDto updateUser(Long userId, UpdateUserRequest request, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify the requester owns the account
        if (!user.getUsername().equals(currentUsername)) {
            throw new BlogApiException(HttpStatus.FORBIDDEN, "You can only update your own profile");
        }

        // Update fields if provided
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            // Ensure new email is not already taken by another user
            if (!user.getEmail().equals(request.getEmail()) &&
                    userRepository.existsByEmail(request.getEmail())) {
                throw new BlogApiException(HttpStatus.BAD_REQUEST, "Email is already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        log.info("User {} updated their profile", currentUsername);
        return mapToDto(updated);
    }

    /**
     * Deletes a user account. Only the account owner can delete their account.
     */
    @Override
    @Transactional
    public void deleteUser(Long userId, String currentUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!user.getUsername().equals(currentUsername)) {
            throw new BlogApiException(HttpStatus.FORBIDDEN, "You can only delete your own account");
        }

        userRepository.delete(user);
        log.info("User {} deleted their account", currentUsername);
    }

    // ── Mapping helper ─────────────────────────────────────────────────────────

    private UserDto mapToDto(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
