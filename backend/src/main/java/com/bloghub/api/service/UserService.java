package com.bloghub.api.service;

import com.bloghub.api.dto.UpdateUserRequest;
import com.bloghub.api.dto.UserDto;

/**
 * Contract for user profile operations.
 */
public interface UserService {

    UserDto getUserById(Long userId);

    UserDto getUserByUsername(String username);

    UserDto updateUser(Long userId, UpdateUserRequest request, String currentUsername);

    void deleteUser(Long userId, String currentUsername);
}
