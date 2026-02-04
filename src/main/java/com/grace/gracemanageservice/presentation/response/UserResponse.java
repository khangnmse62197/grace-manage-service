package com.grace.gracemanageservice.presentation.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * User response DTO - API output
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String role;
    private Boolean active;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Integer age;
    private Long roleId;
    private java.time.LocalDateTime lastCheckInTime;
    private java.time.LocalDateTime lastCheckOutTime;
}
