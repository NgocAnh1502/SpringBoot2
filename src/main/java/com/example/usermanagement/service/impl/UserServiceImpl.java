package com.example.usermanagement.service.impl;

import com.example.usermanagement.client.KeycloakAdminClient;
import com.example.usermanagement.constants.MessageKey;
import com.example.usermanagement.dto.request.UserCreateRequest;
import com.example.usermanagement.dto.request.PasswordUpdateRequest;
import com.example.usermanagement.dto.response.PageResponse;
import com.example.usermanagement.dto.response.UserResponse;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.KeycloakCompensationException;
import com.example.usermanagement.exception.KeycloakIntegrationException;
import com.example.usermanagement.exception.ResourceNotFoundException;
import com.example.usermanagement.mapper.UserMapper;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.service.UserService;
import com.example.usermanagement.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final KeycloakAdminClient keycloakAdminClient;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // Step 0: Standardize data
        String standardizedUsername = request.getUsername() != null ? request.getUsername().trim() : null;
        String standardizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : null;
        
        request.setUsername(standardizedUsername);
        request.setEmail(standardizedEmail);

        // Step 1: Create user in Keycloak first
        String keycloakId = keycloakAdminClient.createUser(
                request.getUsername(), request.getEmail(), request.getPassword());

        // Step 2: Save to database — if this fails, compensate by deleting from Keycloak
        try {
            User entity = userMapper.toEntity(keycloakId, request);
            userRepository.save(entity);
            return userMapper.toResponse(entity);
        } catch (Exception dbException) {
            log.error("Failed to save user to database, compensating by deleting from Keycloak. keycloakId={}",
                    keycloakId, dbException);
            try {
                keycloakAdminClient.deleteUser(keycloakId);
            } catch (Exception compensationException) {
                log.error("CRITICAL: Compensation failed! User exists in Keycloak but not in DB. keycloakId={}",
                        keycloakId, compensationException);
                throw new KeycloakCompensationException(MessageKey.ERROR_COMPENSATION_FAILED, compensationException);
            }
            throw new KeycloakIntegrationException(MessageKey.ERROR_DB_SAVE_FAILED);
        }
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageKey.ERROR_USER_NOT_FOUND, id));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updatePassword(Long id, PasswordUpdateRequest request) {
        // Verify user exists in DB
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageKey.ERROR_USER_NOT_FOUND, id));

        // Update password in Keycloak only (no DB change needed for password)
        if (request.getPassword() != null) {
            keycloakAdminClient.updatePassword(user.getKeycloakId(), request.getPassword());
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Step 1: Verify user exists in DB (save reference for potential compensation)
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageKey.ERROR_USER_NOT_FOUND, id));

        String keycloakId = user.getKeycloakId();

        // Step 2: Delete from Keycloak first
        keycloakAdminClient.deleteUser(keycloakId);

        // Step 3: Delete from database — if this fails, compensate by re-creating in Keycloak
        try {
            userRepository.deleteById(id);
        } catch (Exception dbException) {
            log.error("Failed to delete user from database, compensating by re-creating in Keycloak. keycloakId={}",
                    keycloakId, dbException);
            try {
                keycloakAdminClient.createUser(user.getUsername(), user.getEmail(), null);
            } catch (Exception compensationException) {
                log.error("CRITICAL: Compensation failed! User deleted from Keycloak but still in DB. keycloakId={}",
                        keycloakId, compensationException);
                throw new KeycloakCompensationException(MessageKey.ERROR_COMPENSATION_FAILED, compensationException);
            }
            throw new KeycloakIntegrationException(MessageKey.ERROR_DB_SAVE_FAILED);
        }
    }

    @Override
    public PageResponse<UserResponse> search(String searchName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(
                UserSpecification.hasUsernameLike(searchName), pageable);

        return PageResponse.<UserResponse>builder()
                .content(userPage.getContent().stream()
                        .map(userMapper::toResponse)
                        .toList())
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }
}

