package com.grace.gracemanageservice.infrastructure.persistence.repository;

import com.grace.gracemanageservice.domain.entity.CheckInRecord;
import com.grace.gracemanageservice.domain.repository.CheckInRecordRepository;
import com.grace.gracemanageservice.infrastructure.persistence.entity.CheckInRecordJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CheckInRecordRepository using JPA
 */
@Repository
@RequiredArgsConstructor
public class CheckInRecordRepositoryImpl implements CheckInRecordRepository {

    private final CheckInRecordJpaRepository jpaRepository;

    @Override
    public CheckInRecord save(CheckInRecord checkInRecord) {
        CheckInRecordJpaEntity entity = toJpaEntity(checkInRecord);
        CheckInRecordJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CheckInRecord> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<CheckInRecord> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByTimestampDesc(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<CheckInRecord> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.findByUserIdAndTimestampBetween(userId, start, end).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CheckInRecord> findTopByUserIdOrderByTimestampDesc(Long userId) {
        return jpaRepository.findTopByUserIdOrderByTimestampDesc(userId).map(this::toDomain);
    }

    @Override
    public List<CheckInRecord> findByUserIdAndDate(Long userId, LocalDateTime date) {
        return jpaRepository.findByUserIdAndDate(userId, date).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    // Mapping methods
    private CheckInRecord toDomain(CheckInRecordJpaEntity entity) {
        return CheckInRecord.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(CheckInRecord.CheckInType.valueOf(entity.getType().name()))
                .timestamp(entity.getTimestamp())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .accuracy(entity.getAccuracy())
                .address(entity.getAddress())
                .build();
    }

    private CheckInRecordJpaEntity toJpaEntity(CheckInRecord domain) {
        return CheckInRecordJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .type(CheckInRecordJpaEntity.CheckInType.valueOf(domain.getType().name()))
                .timestamp(domain.getTimestamp())
                .latitude(domain.getLatitude())
                .longitude(domain.getLongitude())
                .accuracy(domain.getAccuracy())
                .address(domain.getAddress())
                .build();
    }
}
