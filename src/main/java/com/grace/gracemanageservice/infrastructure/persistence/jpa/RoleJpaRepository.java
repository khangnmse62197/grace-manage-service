package com.grace.gracemanageservice.infrastructure.persistence.jpa;

import com.grace.gracemanageservice.infrastructure.persistence.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Role - low level database access
 * Internal to infrastructure layer
 */
public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}

