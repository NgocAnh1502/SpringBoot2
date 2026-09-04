package com.example.usermanagement.mapper;

import com.example.usermanagement.dto.request.UserCreateRequest;
import com.example.usermanagement.dto.response.UserResponse;
import com.example.usermanagement.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {
        public UserResponse toResponse(User user) {
                return UserResponse.builder()
                                .id(user.getKeycloakId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .enabled(user.isEnabled())
                                .createdTimestamp(user.getCreatedTimestamp())
                                .build();
        }

        public User toEntity(String keycloakId, UserCreateRequest request) {
                return User.builder()
                                .keycloakId(keycloakId)
                                .username(request.getUsername())
                                .email(request.getEmail())
                                .enabled(true)
                                .createdTimestamp(LocalDateTime.now())
                                .build();
        }
}

