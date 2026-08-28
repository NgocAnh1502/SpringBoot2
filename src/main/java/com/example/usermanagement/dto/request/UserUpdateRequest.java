package com.example.usermanagement.dto.request;

import com.example.usermanagement.constants.MessageKey;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {
    @Size(min = 6, message = MessageKey.ERROR_PASSWORD_SIZE)
    private String password;
}