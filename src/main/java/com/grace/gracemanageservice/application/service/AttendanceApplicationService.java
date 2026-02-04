package com.grace.gracemanageservice.application.service;

import com.grace.gracemanageservice.application.exception.ResourceNotFoundException;
import com.grace.gracemanageservice.domain.entity.CheckInRecord;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.CheckInRecordRepository;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import com.grace.gracemanageservice.presentation.request.CheckInRequest;
import com.grace.gracemanageservice.presentation.response.AttendanceStatusResponse;
import com.grace.gracemanageservice.presentation.response.CheckInRecordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application service for attendance operations
 * Handles check-in/check-out business logic and updates User's last check times
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceApplicationService {

    private final CheckInRecordRepository checkInRecordRepository;
    private final UserRepository userRepository;

    /**
     * Record a check-in for a user
     */
    public CheckInRecordResponse checkIn(CheckInRequest request) {
        log.info("Recording check-in for user: {}", request.userId());

        // Verify user exists
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        LocalDateTime now = LocalDateTime.now();

        // Create check-in record
        CheckInRecord record = CheckInRecord.builder()
                .userId(request.userId())
                .type(CheckInRecord.CheckInType.IN)
                .timestamp(now)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .accuracy(request.accuracy())
                .address(request.address())
                .build();

        CheckInRecord saved = checkInRecordRepository.save(record);

        // Update user's last check-in time
        user.setLastCheckInTime(now);
        userRepository.save(user);

        log.info("Check-in recorded successfully with id: {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Record a check-out for a user
     */
    public CheckInRecordResponse checkOut(CheckInRequest request) {
        log.info("Recording check-out for user: {}", request.userId());

        // Verify user exists
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        LocalDateTime now = LocalDateTime.now();

        // Create check-out record
        CheckInRecord record = CheckInRecord.builder()
                .userId(request.userId())
                .type(CheckInRecord.CheckInType.OUT)
                .timestamp(now)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .accuracy(request.accuracy())
                .address(request.address())
                .build();

        CheckInRecord saved = checkInRecordRepository.save(record);

        // Update user's last check-out time
        user.setLastCheckOutTime(now);
        userRepository.save(user);

        log.info("Check-out recorded successfully with id: {}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Get attendance status for a user
     */
    @Transactional(readOnly = true)
    public AttendanceStatusResponse getStatus(Long userId) {
        log.info("Getting attendance status for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Get today's records
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        List<CheckInRecord> todayRecords = checkInRecordRepository
                .findByUserIdAndTimestampBetween(userId, todayStart, todayEnd);

        int checkInCount = (int) todayRecords.stream()
                .filter(r -> r.getType() == CheckInRecord.CheckInType.IN)
                .count();
        int checkOutCount = (int) todayRecords.stream()
                .filter(r -> r.getType() == CheckInRecord.CheckInType.OUT)
                .count();

        // User is checked in if check-ins > check-outs for today
        boolean isCheckedIn = checkInCount > checkOutCount;

        return new AttendanceStatusResponse(
                isCheckedIn,
                user.getLastCheckInTime(),
                user.getLastCheckOutTime(),
                checkInCount,
                checkOutCount);
    }

    /**
     * Get attendance history for a user
     */
    @Transactional(readOnly = true)
    public List<CheckInRecordResponse> getHistory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting attendance history for user: {} from {} to {}", userId, startDate, endDate);

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<CheckInRecord> records = checkInRecordRepository
                .findByUserIdAndTimestampBetween(userId, startDate, endDate);

        return records.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get today's attendance records for a user
     */
    @Transactional(readOnly = true)
    public List<CheckInRecordResponse> getTodayRecords(Long userId) {
        log.info("Getting today's attendance records for user: {}", userId);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        return getHistory(userId, todayStart, todayEnd);
    }

    private CheckInRecordResponse toResponse(CheckInRecord record) {
        return new CheckInRecordResponse(
                record.getId(),
                record.getUserId(),
                record.getType().name(),
                record.getTimestamp(),
                record.getLatitude(),
                record.getLongitude(),
                record.getAccuracy(),
                record.getAddress());
    }
}
