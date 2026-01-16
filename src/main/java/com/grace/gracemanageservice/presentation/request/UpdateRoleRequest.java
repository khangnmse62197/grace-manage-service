package com.grace.gracemanageservice.presentation.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Update role request DTO - API input for updating a role (PATCH semantics)
 * All fields are optional for partial updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {

    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    private String name;

    @Size(min = 10, max = 500, message = "Role description must be between 10 and 500 characters")
    private String description;

    private Set<String> permissions;
}

