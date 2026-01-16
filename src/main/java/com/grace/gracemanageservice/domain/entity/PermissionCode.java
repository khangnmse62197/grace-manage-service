package com.grace.gracemanageservice.domain.entity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Canonical list of 18 permissions in the system.
 * This is the source of truth for valid permission codes.
 */
public enum PermissionCode {
    VIEW_EMPLOYEES("view_employees"),
    CREATE_EMPLOYEE("create_employee"),
    EDIT_EMPLOYEE("edit_employee"),
    DELETE_EMPLOYEE("delete_employee"),
    VIEW_ROLES("view_roles"),
    CREATE_ROLE("create_role"),
    EDIT_ROLE("edit_role"),
    DELETE_ROLE("delete_role"),
    VIEW_STATISTICS("view_statistics"),
    VIEW_CHECK_IN_OUT("view_check_in_out"),
    MANAGE_CHECK_IN_OUT("manage_check_in_out"),
    MANAGE_INVENTORY("manage_inventory"),
    VIEW_NOTIFICATIONS("view_notifications"),
    MANAGE_NOTIFICATIONS("manage_notifications"),
    EXPORT_DATA("export_data"),
    IMPORT_DATA("import_data"),
    SYSTEM_SETTINGS("system_settings"),
    USER_MANAGEMENT("user_management");

    private final String code;

    PermissionCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Get all permission codes as a set of strings
     */
    public static Set<String> getAllCodes() {
        return Arrays.stream(values())
            .map(PermissionCode::getCode)
            .collect(Collectors.toSet());
    }

    /**
     * Check if a permission code is valid
     */
    public static boolean isValid(String code) {
        return Arrays.stream(values())
            .anyMatch(p -> p.getCode().equals(code));
    }

    /**
     * Get PermissionCode from string code
     */
    public static PermissionCode fromCode(String code) {
        return Arrays.stream(values())
            .filter(p -> p.getCode().equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Invalid permission code: " + code));
    }
}

