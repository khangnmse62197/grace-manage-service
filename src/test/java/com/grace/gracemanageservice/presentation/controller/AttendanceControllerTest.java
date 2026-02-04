package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.service.AttendanceApplicationService;
import com.grace.gracemanageservice.presentation.request.CheckInRequest;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import com.grace.gracemanageservice.presentation.response.AttendanceStatusResponse;
import com.grace.gracemanageservice.presentation.response.CheckInRecordResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceControllerTest {

    @Mock
    private AttendanceApplicationService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    @Test
    void checkIn_shouldReturnCreatedStatus() {
        // Arrange
        CheckInRequest request = new CheckInRequest(1L, 10.776589, 106.696540, 5.0, "Test Address");
        CheckInRecordResponse expectedResponse = new CheckInRecordResponse(
                1L, 1L, "IN", LocalDateTime.now(), 10.776589, 106.696540, 5.0, "Test Address");

        when(attendanceService.checkIn(any(CheckInRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<CheckInRecordResponse>> result = attendanceController.checkIn(request);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("success", result.getBody().getStatus());
        assertEquals(expectedResponse, result.getBody().getData());

        verify(attendanceService).checkIn(request);
    }

    @Test
    void checkOut_shouldReturnCreatedStatus() {
        // Arrange
        CheckInRequest request = new CheckInRequest(1L, 10.776589, 106.696540, 5.0, "Test Address");
        CheckInRecordResponse expectedResponse = new CheckInRecordResponse(
                2L, 1L, "OUT", LocalDateTime.now(), 10.776589, 106.696540, 5.0, "Test Address");

        when(attendanceService.checkOut(any(CheckInRequest.class))).thenReturn(expectedResponse);

        // Act
        ResponseEntity<ApiResponse<CheckInRecordResponse>> result = attendanceController.checkOut(request);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("success", result.getBody().getStatus());
        assertEquals("OUT", result.getBody().getData().type());

        verify(attendanceService).checkOut(request);
    }

    @Test
    void getStatus_shouldReturnCurrentStatus() {
        // Arrange
        Long userId = 1L;
        LocalDateTime checkInTime = LocalDateTime.now().minusHours(2);
        AttendanceStatusResponse expectedStatus = new AttendanceStatusResponse(
                true, checkInTime, null, 1, 0);

        when(attendanceService.getStatus(userId)).thenReturn(expectedStatus);

        // Act
        ResponseEntity<ApiResponse<AttendanceStatusResponse>> result = attendanceController.getStatus(userId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().getData().isCheckedIn());
        assertEquals(1, result.getBody().getData().todayCheckInCount());

        verify(attendanceService).getStatus(userId);
    }

    @Test
    void getTodayRecords_shouldReturnTodayHistory() {
        // Arrange
        Long userId = 1L;
        List<CheckInRecordResponse> expectedRecords = List.of(
                new CheckInRecordResponse(1L, 1L, "IN", LocalDateTime.now().minusHours(2),
                        10.776589, 106.696540, 5.0, "Office"),
                new CheckInRecordResponse(2L, 1L, "OUT", LocalDateTime.now().minusHours(1),
                        10.776589, 106.696540, 5.0, "Office"));

        when(attendanceService.getTodayRecords(userId)).thenReturn(expectedRecords);

        // Act
        ResponseEntity<ApiResponse<List<CheckInRecordResponse>>> result = attendanceController.getTodayRecords(userId);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().getData().size());

        verify(attendanceService).getTodayRecords(userId);
    }

    @Test
    void getHistory_shouldReturnHistoryWithDefaults() {
        // Arrange
        Long userId = 1L;
        List<CheckInRecordResponse> expectedRecords = List.of(
                new CheckInRecordResponse(1L, 1L, "IN", LocalDateTime.now().minusDays(5),
                        10.776589, 106.696540, 5.0, "Office"));

        when(attendanceService.getHistory(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expectedRecords);

        // Act
        ResponseEntity<ApiResponse<List<CheckInRecordResponse>>> result = attendanceController.getHistory(userId, null,
                null);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().getData().size());

        verify(attendanceService).getHistory(eq(userId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
}
