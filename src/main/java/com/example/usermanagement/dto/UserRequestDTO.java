package com.example.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDTO {
    @NotBlank(message = "{user.username.not-blank}")
    @Size(min = 3, max = 50, message = "{user.username.size}")
    private String username;

    @NotBlank(message = "{user.password.not-blank}")
    @Size(min = 6, message = "{user.password.size}")
    private String password;

    @NotBlank(message = "{user.email.not-blank}")
    @Email(message = "{user.email.invalid}")
    private String email;
}
