package com.example.usermanagement.mapper;

import com.example.usermanagement.dto.response.UserResponse;
import com.example.usermanagement.entity.User;
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

        public UserResponse toResponse(User user) {
                return UserResponse.builder()
                                .id(user.getKeycloakId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .enabled(user.isEnabled())
                                .createdTimestamp(user.getCreatedTimestamp())
                                .build();
        }

        public User toEntity(String keycloakId, JsonNode keycloakUser) {
                return User.builder()
                                .keycloakId(keycloakId)
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

