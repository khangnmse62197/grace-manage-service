package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.service.AttendanceApplicationService;
import com.grace.gracemanageservice.presentation.request.CheckInRequest;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import com.grace.gracemanageservice.presentation.response.AttendanceStatusResponse;
import com.grace.gracemanageservice.presentation.response.CheckInRecordResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * REST controller for attendance operations (check-in/check-out)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceApplicationService attendanceService;

    /**
     * Record a check-in for a user
     */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<CheckInRecordResponse>> checkIn(
            @Valid @RequestBody CheckInRequest request) {

        log.info("Check-in request for user: {}", request.userId());

        CheckInRecordResponse response = attendanceService.checkIn(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Check-in recorded successfully"));
    }

    /**
     * Record a check-out for a user
     */
    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<CheckInRecordResponse>> checkOut(
            @Valid @RequestBody CheckInRequest request) {

        log.info("Check-out request for user: {}", request.userId());

        CheckInRecordResponse response = attendanceService.checkOut(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Check-out recorded successfully"));
    }

    /**
     * Get current attendance status for a user
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<ApiResponse<AttendanceStatusResponse>> getStatus(
            @PathVariable Long userId) {

        log.info("Getting attendance status for user: {}", userId);

        AttendanceStatusResponse response = attendanceService.getStatus(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get attendance history for a user within a date range
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<CheckInRecordResponse>>> getHistory(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Getting attendance history for user: {}", userId);

        // Default to last 30 days if not specified
        LocalDateTime start = startDate != null
                ? startDate.atStartOfDay()
                : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = endDate != null
                ? endDate.atTime(LocalTime.MAX)
                : LocalDate.now().atTime(LocalTime.MAX);

        List<CheckInRecordResponse> response = attendanceService.getHistory(userId, start, end);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get today's attendance records for a user
     */
    @GetMapping("/today/{userId}")
    public ResponseEntity<ApiResponse<List<CheckInRecordResponse>>> getTodayRecords(
            @PathVariable Long userId) {

        log.info("Getting today's attendance records for user: {}", userId);

        List<CheckInRecordResponse> response = attendanceService.getTodayRecords(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
