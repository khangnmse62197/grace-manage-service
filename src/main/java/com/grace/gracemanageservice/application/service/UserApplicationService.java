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
    private final com.grace.gracemanageservice.domain.usecase.UpdateUserUseCase updateUserUseCase;

    public UserDTO updateUser(Long id, com.grace.gracemanageservice.presentation.request.UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = updateUserUseCase.execute(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getRole(),
                request.getDateOfBirth(),
                request.getRoleId(),
                request.getPassword(),
                request.getActive(),
                request.getLastCheckInTime(),
                request.getLastCheckOutTime());

        return userMapper.toDTO(user);
    }

    public UserDTO createUser(String username, String email, String firstName, String lastName, String password) {
        return createUser(username, email, firstName, lastName, password, "user", null, null);
    }

    public UserDTO createUser(String username, String email, String firstName, String lastName, String password,
            String role, java.time.LocalDate dateOfBirth, Long roleId) {
        log.info("Creating user with email: {}", email);

        User user = createUserUseCase.execute(username, email, firstName, lastName, password, role, dateOfBirth,
                roleId);

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

    public java.util.List<UserDTO> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.deleteById(id);
        log.info("User deleted successfully");
    }
}
