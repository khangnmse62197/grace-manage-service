package com.grace.gracemanageservice.application.mapper;

import com.grace.gracemanageservice.application.dto.UserDTO;
import com.grace.gracemanageservice.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * User mapper - converts between User entity and UserDTO
 * Uses MapStruct for automatic implementation
 * Ignores password field for security reasons
 */
@Mapper(componentModel = "spring", unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDTO toDTO(User user);

    User toEntity(UserDTO userDTO);
}

