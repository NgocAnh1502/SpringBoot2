package com.example.usermanagement.mapper;

import com.example.usermanagement.dto.response.UserResponse;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class UserMapper {
        public UserResponse toResponse(JsonNode keycloakUser) {
                return UserResponse.builder()
                                .id(keycloakUser.get("id").asString())
                                .username(keycloakUser.get("username").asString())
                                .email(keycloakUser.hasNonNull("email") ? keycloakUser.get("email").asString() : null)
                                .enabled(keycloakUser.get("enabled").asBoolean())
                                .createdTimestamp(keycloakUser.hasNonNull("createdTimestamp")
                                                ? LocalDateTime.ofInstant(
                                                                Instant.ofEpochMilli(keycloakUser
                                                                                .get("createdTimestamp").asLong()),
                                                                ZoneId.systemDefault())
                                                : null)
                                .build();
        }
}
