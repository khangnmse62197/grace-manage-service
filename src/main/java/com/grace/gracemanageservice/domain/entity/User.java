package com.grace.gracemanageservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * User domain entity - core business object
 * Framework independent, represents the user concept
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean active;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    // Employee management fields
    private LocalDate dateOfBirth;
    private Long roleId;
    private java.time.LocalDateTime lastCheckInTime;
    private java.time.LocalDateTime lastCheckOutTime;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return active != null && active;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }
}
