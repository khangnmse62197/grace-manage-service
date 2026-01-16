package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.service.RoleApplicationService;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Permission REST controller - handles HTTP requests for permissions
 * Provides the list of available permissions for the UI
 * Requires ADMIN role
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final RoleApplicationService roleApplicationService;

    /**
     * Get all available permissions
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Set<String>>> getAllPermissions() {
        log.info("Getting all available permissions");

        Set<String> permissions = roleApplicationService.getAllPermissions();

        return ResponseEntity.ok(ApiResponse.success(permissions, "Available permissions retrieved successfully"));
    }
}

