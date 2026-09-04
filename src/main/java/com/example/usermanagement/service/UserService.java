package com.example.usermanagement.service;

import com.example.usermanagement.dto.request.UserCreateRequest;
import com.example.usermanagement.dto.request.PasswordUpdateRequest;
import com.example.usermanagement.dto.response.PageResponse;
import com.example.usermanagement.dto.response.UserResponse;

public interface UserService {
    UserResponse createUser(UserCreateRequest requestDTO);
    UserResponse updatePassword(Long id, PasswordUpdateRequest requestDTO);
    void deleteUser(Long id);
    UserResponse getUserById(Long id);
    PageResponse<UserResponse> search(String searchName, int page, int size);
}
