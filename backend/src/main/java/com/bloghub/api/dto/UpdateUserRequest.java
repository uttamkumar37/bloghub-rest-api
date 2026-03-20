package com.bloghub.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for updating user profile information.
 */
@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Email must be valid")
    private String email;

    /** If provided, the user's password will be changed */
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;
}
