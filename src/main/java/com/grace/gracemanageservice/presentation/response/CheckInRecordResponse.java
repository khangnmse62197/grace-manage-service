package com.grace.gracemanageservice.presentation.response;

import java.time.LocalDateTime;

/**
 * Response record for a single check-in/check-out record
 */
public record CheckInRecordResponse(
        Long id,
        Long userId,
        String type,
        LocalDateTime timestamp,
        Double latitude,
        Double longitude,
        Double accuracy,
        String address) {
}
