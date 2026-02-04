package com.grace.gracemanageservice.domain.repository;

import com.grace.gracemanageservice.domain.entity.CheckInRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CheckInRecord entity
 */
public interface CheckInRecordRepository {

    CheckInRecord save(CheckInRecord checkInRecord);

    Optional<CheckInRecord> findById(Long id);

    List<CheckInRecord> findByUserId(Long userId);

    List<CheckInRecord> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    /**
     * Find the most recent check-in record for a user
     */
    Optional<CheckInRecord> findTopByUserIdOrderByTimestampDesc(Long userId);

    /**
     * Find records for a user on a specific date
     */
    List<CheckInRecord> findByUserIdAndDate(Long userId, LocalDateTime date);

    void deleteById(Long id);
}
