package com.bloghub.service;

import com.bloghub.repository.UserRepository;
import com.bloghub.web.dto.PageResponse;
import com.bloghub.web.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final UserRepository users;

    public AdminService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserDto> listUsers(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<UserDto> mapped = users.findAll(pageable)
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setEmail(user.getEmail());
                    dto.setDisplayName(user.getDisplayName());
                    dto.setRole(user.getRole().getName().name());
                    return dto;
                });
        return PageResponse.fromPage(mapped);
    }
}

