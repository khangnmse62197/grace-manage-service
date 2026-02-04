package com.grace.gracemanageservice.presentation.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request record for check-in/check-out operations
 */
public record CheckInRequest(
        @NotNull(message = "User ID is required") Long userId,

        Double latitude,

        Double longitude,

        Double accuracy,

        String address) {
}
