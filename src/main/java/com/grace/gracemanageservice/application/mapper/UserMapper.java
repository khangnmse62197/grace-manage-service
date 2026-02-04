package com.grace.gracemanageservice.application.mapper;

import com.grace.gracemanageservice.application.dto.UserDTO;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.presentation.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

/**
 * User mapper - converts between User entity, UserDTO and UserResponse
 * Uses MapStruct for automatic implementation
 * Ignores password field for security reasons
 */
@Mapper(componentModel = "spring", unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface UserMapper {

    UserDTO toDTO(User user);

    User toEntity(UserDTO userDTO);

    @Mapping(target = "fullName", expression = "java(userDTO.getFirstName() + \" \" + userDTO.getLastName())")
    @Mapping(target = "age", expression = "java(userDTO.getDateOfBirth() != null ? java.time.Period.between(userDTO.getDateOfBirth(), java.time.LocalDate.now()).getYears() : null)")
    UserResponse toResponse(UserDTO userDTO);
}
