package com.grace.gracemanageservice.infrastructure.persistence;

import com.grace.gracemanageservice.domain.entity.Role;
import com.grace.gracemanageservice.domain.repository.RoleRepository;
import com.grace.gracemanageservice.infrastructure.persistence.jpa.RoleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Role repository implementation - concrete implementation using JPA
 * Implements domain repository interface
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;

    @Override
    public List<Role> findAll() {
        return roleJpaRepository.findAll()
            .stream()
            .map(RoleEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roleJpaRepository.findById(id)
            .map(RoleEntity::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name)
            .map(RoleEntity::toDomain);
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity = RoleEntity.fromDomain(role);
        RoleEntity saved = roleJpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public void deleteById(Long id) {
        roleJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return roleJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return roleJpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, Long id) {
        return roleJpaRepository.existsByNameAndIdNot(name, id);
    }
}

