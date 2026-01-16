package com.grace.gracemanageservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Role DTO - for internal data transfer between layers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;
    private String name;
    private String description;
    private Set<String> permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

