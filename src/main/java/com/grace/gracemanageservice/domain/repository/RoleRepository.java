package com.grace.gracemanageservice.domain.repository;

import com.grace.gracemanageservice.domain.entity.Role;

import java.util.List;
import java.util.Optional;

/**
 * Role repository interface - defines contracts for data access
 * Implemented by infrastructure layer
 */
public interface RoleRepository {

    List<Role> findAll();

    Optional<Role> findById(Long id);

    Optional<Role> findByName(String name);

    Role save(Role role);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}

