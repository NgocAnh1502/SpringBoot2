package com.example.usermanagement.dto.request;

import com.example.usermanagement.constants.MessageKey;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {
    @NotBlank(message = MessageKey.ERROR_USERNAME_NOT_BLANK)
    @Size(min = 3, max = 50, message = MessageKey.ERROR_USERNAME_SIZE)
    private String username;

    @NotBlank(message = MessageKey.ERROR_PASSWORD_NOT_BLANK)
    @Size(min = 6, message = MessageKey.ERROR_PASSWORD_SIZE)
    private String password;

    @NotBlank(message = MessageKey.ERROR_EMAIL_NOT_BLANK)
    @Email(message = MessageKey.ERROR_EMAIL_INVALID)
    private String email;
}
