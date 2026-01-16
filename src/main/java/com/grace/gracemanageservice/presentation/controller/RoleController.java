package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.dto.RoleDTO;
import com.grace.gracemanageservice.application.mapper.RoleMapper;
import com.grace.gracemanageservice.application.service.RoleApplicationService;
import com.grace.gracemanageservice.presentation.request.CreateRoleRequest;
import com.grace.gracemanageservice.presentation.request.UpdateRoleRequest;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import com.grace.gracemanageservice.presentation.response.RoleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Role REST controller - handles HTTP requests for role management
 * All endpoints require ADMIN role
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleApplicationService roleApplicationService;
    private final RoleMapper roleMapper;

    /**
     * Get all roles
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        log.info("Getting all roles");

        List<RoleDTO> roleDTOs = roleApplicationService.getAllRoles();
        List<RoleResponse> responses = roleMapper.toResponseList(roleDTOs);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get a role by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        log.info("Getting role with id: {}", id);

        RoleDTO roleDTO = roleApplicationService.getRoleById(id);
        RoleResponse response = roleMapper.toResponse(roleDTO);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Create a new role
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        log.info("Creating role with name: {}", request.getName());

        RoleDTO roleDTO = roleApplicationService.createRole(
            request.getName(),
            request.getDescription(),
            request.getPermissions()
        );

        RoleResponse response = roleMapper.toResponse(roleDTO);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "Role created successfully"));
    }

    /**
     * Update a role (PATCH semantics - partial update)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("Updating role with id: {}", id);

        RoleDTO roleDTO = roleApplicationService.updateRole(
            id,
            request.getName(),
            request.getDescription(),
            request.getPermissions()
        );

        RoleResponse response = roleMapper.toResponse(roleDTO);
        return ResponseEntity.ok(ApiResponse.success(response, "Role updated successfully"));
    }

    /**
     * Delete a role by id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        log.info("Deleting role with id: {}", id);

        roleApplicationService.deleteRole(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }
}

