package com.grace.gracemanageservice.domain.repository;

import com.grace.gracemanageservice.domain.entity.User;

import java.util.Optional;

/**
 * User repository interface - defines contracts for data access
 * Implemented by infrastructure layer
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    void deleteById(Long id);

    long count();
}

