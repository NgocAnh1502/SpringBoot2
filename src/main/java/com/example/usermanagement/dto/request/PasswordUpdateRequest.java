package com.example.usermanagement.dto.request;

import com.example.usermanagement.constants.MessageKey;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordUpdateRequest {
    @NotBlank(message = MessageKey.ERROR_PASSWORD_NOT_BLANK)
    @Size(min = 6, message = MessageKey.ERROR_PASSWORD_SIZE)
    private String password;
}