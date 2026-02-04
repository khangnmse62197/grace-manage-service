package com.grace.gracemanageservice.infrastructure.persistence.repository;

import com.grace.gracemanageservice.infrastructure.persistence.entity.CheckInRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for CheckInRecord
 */
@Repository
public interface CheckInRecordJpaRepository extends JpaRepository<CheckInRecordJpaEntity, Long> {

    List<CheckInRecordJpaEntity> findByUserId(Long userId);

    List<CheckInRecordJpaEntity> findByUserIdAndTimestampBetween(Long userId, LocalDateTime start, LocalDateTime end);

    Optional<CheckInRecordJpaEntity> findTopByUserIdOrderByTimestampDesc(Long userId);

    @Query("SELECT c FROM CheckInRecordJpaEntity c WHERE c.userId = :userId AND CAST(c.timestamp AS date) = CAST(:date AS date)")
    List<CheckInRecordJpaEntity> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    List<CheckInRecordJpaEntity> findByUserIdOrderByTimestampDesc(Long userId);
}
