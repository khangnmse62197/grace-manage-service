package com.grace.gracemanageservice.application.service;

import com.grace.gracemanageservice.application.dto.RoleDTO;
import com.grace.gracemanageservice.application.mapper.RoleMapper;
import com.grace.gracemanageservice.domain.entity.PermissionCode;
import com.grace.gracemanageservice.domain.entity.Role;
import com.grace.gracemanageservice.domain.usecase.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Role application service - orchestrates use cases
 * Acts as a bridge between presentation and domain layers
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoleApplicationService {

    private final ListRolesUseCase listRolesUseCase;
    private final GetRoleUseCase getRoleUseCase;
    private final CreateRoleUseCase createRoleUseCase;
    private final UpdateRoleUseCase updateRoleUseCase;
    private final DeleteRoleUseCase deleteRoleUseCase;
    private final RoleMapper roleMapper;

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        log.info("Getting all roles");
        List<Role> roles = listRolesUseCase.execute();
        return roleMapper.toDTOList(roles);
    }

    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        log.info("Getting role by id: {}", id);
        Role role = getRoleUseCase.execute(id);
        return roleMapper.toDTO(role);
    }

    public RoleDTO createRole(String name, String description, Set<String> permissions) {
        log.info("Creating role with name: {}", name);
        Role role = createRoleUseCase.execute(name, description, permissions);
        log.info("Role created successfully with id: {}", role.getId());
        return roleMapper.toDTO(role);
    }

    public RoleDTO updateRole(Long id, String name, String description, Set<String> permissions) {
        log.info("Updating role with id: {}", id);
        Role role = updateRoleUseCase.execute(id, name, description, permissions);
        log.info("Role updated successfully with id: {}", role.getId());
        return roleMapper.toDTO(role);
    }

    public void deleteRole(Long id) {
        log.info("Deleting role with id: {}", id);
        deleteRoleUseCase.execute(id);
        log.info("Role deleted successfully");
    }

    /**
     * Get all available permission codes
     */
    @Transactional(readOnly = true)
    public Set<String> getAllPermissions() {
        return PermissionCode.getAllCodes();
    }
}

