package com.grace.gracemanageservice.infrastructure.persistence.jpa;

import com.grace.gracemanageservice.infrastructure.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository - low level database access
 * Internal to infrastructure layer
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    java.util.Optional<UserEntity> findByEmail(String email);

    java.util.Optional<UserEntity> findByUsername(String username);
}

