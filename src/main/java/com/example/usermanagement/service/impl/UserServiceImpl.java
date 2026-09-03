package com.example.usermanagement.service.impl;

import com.example.usermanagement.client.KeycloakAdminClient;
import com.example.usermanagement.dto.request.UserCreateRequest;
import com.example.usermanagement.dto.request.PasswordUpdateRequest;
import com.example.usermanagement.dto.response.PageResponse;
import com.example.usermanagement.dto.response.UserResponse;
import com.example.usermanagement.mapper.UserMapper;
import com.example.usermanagement.service.UserService;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final KeycloakAdminClient keycloakAdminClient;

    @Override
    public UserResponse createUser(UserCreateRequest request) {
        String userId = keycloakAdminClient.createUser(
                request.getUsername(), request.getEmail(), request.getPassword());
        JsonNode created = keycloakAdminClient.getUserById(userId);
        return userMapper.toResponse(created);
    }

    @Override
    public UserResponse getUserById(String id) {
        return userMapper.toResponse(keycloakAdminClient.getUserById(id));
    }

    @Override
    public UserResponse updateUser(String id, PasswordUpdateRequest request) {
        if (request.getPassword() != null) {
            keycloakAdminClient.updatePassword(id, request.getPassword());
        }
        return userMapper.toResponse(keycloakAdminClient.getUserById(id));
    }

    @Override
    public void deleteUser(String id) {
        keycloakAdminClient.deleteUser(id);

    }

    @Override
    public PageResponse<UserResponse> search(String searchName, int page, int size) {
        int first = page * size; // Keycloak dùng offset (first) thay vì số trang
        List<JsonNode> rawUsers = keycloakAdminClient.searchUsers(searchName, first, size);
        long total = keycloakAdminClient.countUsers(searchName);

        List<UserResponse> content = rawUsers.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / size);

        return PageResponse.<UserResponse>builder()
                .content(content)
                .pageNumber(page)
                .pageSize(size)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }
}
