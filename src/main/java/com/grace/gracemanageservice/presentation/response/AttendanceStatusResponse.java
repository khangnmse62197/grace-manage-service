package com.grace.gracemanageservice.presentation.response;

import java.time.LocalDateTime;

/**
 * Response record for attendance status
 */
public record AttendanceStatusResponse(
        boolean isCheckedIn,
        LocalDateTime lastCheckInTime,
        LocalDateTime lastCheckOutTime,
        int todayCheckInCount,
        int todayCheckOutCount) {
}
