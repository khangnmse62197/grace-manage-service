package com.grace.gracemanageservice.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Create role request DTO - API input for creating a new role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {

    @NotBlank(message = "Role name is required")
    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Role description is required")
    @Size(min = 10, max = 500, message = "Role description must be between 10 and 500 characters")
    private String description;

    @NotEmpty(message = "At least one permission is required")
    private Set<String> permissions;
}

