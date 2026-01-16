package com.grace.gracemanageservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Role domain entity - core business object
 * Framework independent, represents the role concept with permissions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Long id;
    private String name;
    private String description;

    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Check if this role has a specific permission
     */
    public boolean hasPermission(String permissionCode) {
        return permissions != null && permissions.contains(permissionCode);
    }

    /**
     * Check if this role has a specific permission
     */
    public boolean hasPermission(PermissionCode permission) {
        return hasPermission(permission.getCode());
    }

    /**
     * Check if this is the system ADMIN role
     */
    public boolean isAdminRole() {
        return "ADMIN".equalsIgnoreCase(name);
    }
}

