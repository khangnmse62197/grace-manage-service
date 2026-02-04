package com.grace.gracemanageservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CheckInRecord domain entity - represents a check-in or check-out event
 * Stores location data and timestamp for attendance tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRecord {
    private Long id;
    private Long userId;
    private CheckInType type;
    private LocalDateTime timestamp;

    // Location data
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private String address;

    public enum CheckInType {
        IN, OUT
    }
}
