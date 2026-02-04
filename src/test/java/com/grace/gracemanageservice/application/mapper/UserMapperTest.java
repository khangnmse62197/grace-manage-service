package com.grace.gracemanageservice.application.mapper;

import com.grace.gracemanageservice.application.dto.UserDTO;
import com.grace.gracemanageservice.presentation.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toResponse_shouldMapAllFieldsAndCalculateAge() {
        // Arrange
        LocalDate dateOfBirth = LocalDate.of(1990, 5, 15);
        int expectedAge = Period.between(dateOfBirth, LocalDate.now()).getYears();

        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .username("test")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(dateOfBirth)
                .roleId(5L)
                .lastCheckInTime(LocalDateTime.now().minusHours(8))
                .lastCheckOutTime(LocalDateTime.now())
                .build();

        // Act
        UserResponse response = mapper.toResponse(userDTO);

        // Assert
        assertEquals("John Doe", response.getFullName());
        assertEquals(expectedAge, response.getAge());
        assertEquals(5L, response.getRoleId());
        assertEquals(userDTO.getLastCheckInTime(), response.getLastCheckInTime());
        assertEquals(userDTO.getLastCheckOutTime(), response.getLastCheckOutTime());
    }

    @Test
    void toResponse_shouldHandleNullDateOfBirth() {
        // Arrange
        UserDTO userDTO = UserDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(null)
                .build();

        // Act
        UserResponse response = mapper.toResponse(userDTO);

        // Assert
        assertEquals("John Doe", response.getFullName());
        assertNull(response.getAge());
    }
}
