package com.grace.gracemanageservice.infrastructure.persistence;

import com.grace.gracemanageservice.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User JPA entity - database representation
 * Separate from domain User entity for database concerns
 */
@Entity
@Table(name = "T_USER")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at")
    private Long createdAt;

    @Column(name = "updated_at")
    private Long updatedAt;

    // Conversion methods between Entity and Domain
    public static UserEntity fromDomain(User user) {
        return UserEntity.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .password(user.getPassword())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .active(user.getActive())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    public User toDomain() {
        return User.builder()
            .id(this.id)
            .username(this.username)
            .email(this.email)
            .password(this.password)
            .firstName(this.firstName)
            .lastName(this.lastName)
            .role(this.role)
            .active(this.active)
            .createdAt(this.createdAt)
            .updatedAt(this.updatedAt)
            .build();
    }
}

