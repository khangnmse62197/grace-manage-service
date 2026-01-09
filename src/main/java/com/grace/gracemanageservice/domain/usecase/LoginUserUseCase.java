package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ValidationException;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Login user use case - validates credentials and returns authenticated user
 */
@Component
public class LoginUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(String username, String password) {
        // Validate input
        if (username == null || username.isBlank()) {
            throw new ValidationException("username", "Username is required");
        }
        if (password == null || password.isBlank()) {
            throw new ValidationException("password", "Password is required");
        }

        // Find user by username
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ValidationException("credentials", "Invalid username or password"));

        // Check if user is active
        if (!user.isActive()) {
            throw new ValidationException("account", "Account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("credentials", "Invalid username or password");
        }

        return user;
    }
}

