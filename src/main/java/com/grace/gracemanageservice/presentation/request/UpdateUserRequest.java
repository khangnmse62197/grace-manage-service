package com.grace.gracemanageservice.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Update user request DTO - API input
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String firstName;

    private String lastName;

    @Email(message = "Email should be valid")
    private String email;

    @Pattern(regexp = "^(admin|user|viewer)$", message = "Role must be one of: admin, user, viewer")
    private String role;

    private LocalDate dateOfBirth;

    private Long roleId;

    private java.time.LocalDateTime lastCheckInTime;

    private java.time.LocalDateTime lastCheckOutTime;

    private String password; // Optional password update

    private Boolean active; // Allow activating/deactivating
}
