package com.grace.gracemanageservice.infrastructure.persistence;

import com.grace.gracemanageservice.domain.entity.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Role JPA entity - database representation
 * Separate from domain Role entity for database concerns
 */
@Entity
@Table(name = "T_ROLE")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "T_ROLE_PERMISSION",
        joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permission_code", length = 100)
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Conversion methods between Entity and Domain
    public static RoleEntity fromDomain(Role role) {
        return RoleEntity.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .permissions(role.getPermissions() != null ? new HashSet<>(role.getPermissions()) : new HashSet<>())
            .createdAt(role.getCreatedAt())
            .updatedAt(role.getUpdatedAt())
            .build();
    }

    public Role toDomain() {
        return Role.builder()
            .id(this.id)
            .name(this.name)
            .description(this.description)
            .permissions(this.permissions != null ? new HashSet<>(this.permissions) : new HashSet<>())
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}

