package com.grace.gracemanageservice.application.service;

import com.grace.gracemanageservice.application.dto.UserDTO;
import com.grace.gracemanageservice.application.exception.ResourceNotFoundException;
import com.grace.gracemanageservice.application.mapper.UserMapper;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import com.grace.gracemanageservice.domain.usecase.CreateUserUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User application service - orchestrates use cases
 * Acts as a bridge between presentation and domain layers
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserApplicationService {

    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserDTO createUser(String username, String email, String firstName, String lastName, String password) {
        return createUser(username, email, firstName, lastName, password, "user");
    }

    public UserDTO createUser(String username, String email, String firstName, String lastName, String password, String role) {
        log.info("Creating user with email: {}", email);

        User user = createUserUseCase.execute(username, email, firstName, lastName, password, role);

        log.info("User created successfully with id: {}", user.getId());
        return userMapper.toDTO(user);
    }

    public UserDTO getUserById(Long id) {
        log.info("Getting user by id: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return userMapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        log.info("Getting user by email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return userMapper.toDTO(user);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.deleteById(id);
        log.info("User deleted successfully");
    }
}

