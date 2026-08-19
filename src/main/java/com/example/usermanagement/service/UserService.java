package com.example.usermanagement.service;

import com.example.usermanagement.dto.PageResponseDTO;
import com.example.usermanagement.dto.UserRequestDTO;
import com.example.usermanagement.dto.UserResponseDTO;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO requestDTO);
    UserResponseDTO updateUser(Long id, UserRequestDTO requestDTO);
    void deleteUser(Long id);
    UserResponseDTO getUserById(Long id);
    PageResponseDTO<UserResponseDTO> search(String searchName, int page, int size, String sortBy, String direction);
}
