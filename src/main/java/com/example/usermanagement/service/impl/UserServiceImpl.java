package com.example.usermanagement.service.impl;

import com.example.usermanagement.dto.PageResponseDTO;
import com.example.usermanagement.dto.UserRequestDTO;
import com.example.usermanagement.dto.UserResponseDTO;
import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.DuplicateResourceException;
import com.example.usermanagement.exception.ResourceNotFoundException;
import com.example.usermanagement.mapper.UserMapper;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.service.UserService;
import com.example.usermanagement.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request){
        if(userRepository.existsByUsername(request.getUsername())){
            throw new DuplicateResourceException(
                    messageSource.getMessage("error.username.duplicate",
                            new Object[]{request.getUsername()}, LocaleContextHolder.getLocale())
            );
        }
        if(userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException(
                    messageSource.getMessage("error.email.duplicate",
                            new Object[]{request.getEmail()}, LocaleContextHolder.getLocale())
            );
        }
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        return userMapper.toResponseDTO(saved);
    }

    @Override
    public UserResponseDTO getUserById(Long id){
        User user = findUserOrThrow(id);
        return userMapper.toResponseDTO(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO request){
        User user = findUserOrThrow(id);

        if(request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if(request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if(request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id){
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    @Override
    public PageResponseDTO<UserResponseDTO> search(String searchName, int page, int size, String sortBy, String direction){
        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> resultPage = userRepository.findAll(
                UserSpecification.hasUsernameLike(searchName), pageable);
        List<UserResponseDTO> content = resultPage.getContent().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
        return PageResponseDTO.<UserResponseDTO>builder()
                .content(content)
                .pageNumber(resultPage.getNumber())
                .pageSize(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .last(resultPage.isLast())
                .build();
    }

    private User findUserOrThrow(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.user.not-found",
                        new Object[]{id}, LocaleContextHolder.getLocale())
                ));
    }
}
