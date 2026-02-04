package com.grace.gracemanageservice.application.service;

import com.grace.gracemanageservice.application.exception.ResourceNotFoundException;
import com.grace.gracemanageservice.domain.entity.CheckInRecord;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.CheckInRecordRepository;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import com.grace.gracemanageservice.presentation.request.CheckInRequest;
import com.grace.gracemanageservice.presentation.response.AttendanceStatusResponse;
import com.grace.gracemanageservice.presentation.response.CheckInRecordResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceApplicationServiceTest {

    @Mock
    private CheckInRecordRepository checkInRecordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AttendanceApplicationService attendanceService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void checkIn_shouldSaveRecordAndUpdateUser() {
        // Arrange
        CheckInRequest request = new CheckInRequest(1L, 10.776589, 106.696540, 5.0, "Test Address");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(checkInRecordRepository.save(any(CheckInRecord.class))).thenAnswer(invocation -> {
            CheckInRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        CheckInRecordResponse response = attendanceService.checkIn(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(1L, response.userId());
        assertEquals("IN", response.type());
        assertEquals(10.776589, response.latitude());

        // Verify user's lastCheckInTime was updated
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getLastCheckInTime());
    }

    @Test
    void checkOut_shouldSaveRecordAndUpdateUser() {
        // Arrange
        CheckInRequest request = new CheckInRequest(1L, 10.776589, 106.696540, 5.0, "Test Address");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(checkInRecordRepository.save(any(CheckInRecord.class))).thenAnswer(invocation -> {
            CheckInRecord record = invocation.getArgument(0);
            record.setId(2L);
            return record;
        });
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        CheckInRecordResponse response = attendanceService.checkOut(request);

        // Assert
        assertNotNull(response);
        assertEquals(2L, response.id());
        assertEquals("OUT", response.type());

        // Verify user's lastCheckOutTime was updated
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertNotNull(userCaptor.getValue().getLastCheckOutTime());
    }

    @Test
    void checkIn_shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        CheckInRequest request = new CheckInRequest(999L, 10.776589, 106.696540, 5.0, "Test Address");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.checkIn(request));
    }

    @Test
    void getStatus_shouldReturnCorrectStatus() {
        // Arrange
        LocalDateTime checkInTime = LocalDateTime.now().minusHours(2);
        testUser.setLastCheckInTime(checkInTime);

        List<CheckInRecord> todayRecords = List.of(
                CheckInRecord.builder()
                        .id(1L)
                        .userId(1L)
                        .type(CheckInRecord.CheckInType.IN)
                        .timestamp(checkInTime)
                        .build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(checkInRecordRepository.findByUserIdAndTimestampBetween(eq(1L), any(), any()))
                .thenReturn(todayRecords);

        // Act
        AttendanceStatusResponse status = attendanceService.getStatus(1L);

        // Assert
        assertTrue(status.isCheckedIn());
        assertEquals(1, status.todayCheckInCount());
        assertEquals(0, status.todayCheckOutCount());
        assertEquals(checkInTime, status.lastCheckInTime());
    }

    @Test
    void getStatus_shouldReturnNotCheckedInWhenEqualCheckInsAndOuts() {
        // Arrange
        List<CheckInRecord> todayRecords = List.of(
                CheckInRecord.builder()
                        .id(1L)
                        .userId(1L)
                        .type(CheckInRecord.CheckInType.IN)
                        .timestamp(LocalDateTime.now().minusHours(2))
                        .build(),
                CheckInRecord.builder()
                        .id(2L)
                        .userId(1L)
                        .type(CheckInRecord.CheckInType.OUT)
                        .timestamp(LocalDateTime.now().minusHours(1))
                        .build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(checkInRecordRepository.findByUserIdAndTimestampBetween(eq(1L), any(), any()))
                .thenReturn(todayRecords);

        // Act
        AttendanceStatusResponse status = attendanceService.getStatus(1L);

        // Assert
        assertFalse(status.isCheckedIn());
        assertEquals(1, status.todayCheckInCount());
        assertEquals(1, status.todayCheckOutCount());
    }

    @Test
    void getTodayRecords_shouldReturnTodayHistory() {
        // Arrange
        List<CheckInRecord> todayRecords = List.of(
                CheckInRecord.builder()
                        .id(1L)
                        .userId(1L)
                        .type(CheckInRecord.CheckInType.IN)
                        .timestamp(LocalDateTime.now().minusHours(2))
                        .latitude(10.776589)
                        .longitude(106.696540)
                        .build());

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(checkInRecordRepository.findByUserIdAndTimestampBetween(eq(1L), any(), any()))
                .thenReturn(todayRecords);

        // Act
        List<CheckInRecordResponse> records = attendanceService.getTodayRecords(1L);

        // Assert
        assertEquals(1, records.size());
        assertEquals("IN", records.get(0).type());
    }
}
