package com.grace.gracemanageservice.application.mapper;

import com.grace.gracemanageservice.application.dto.RoleDTO;
import com.grace.gracemanageservice.domain.entity.Role;
import com.grace.gracemanageservice.presentation.response.RoleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import java.util.List;

/**
 * Role mapper - converts between Role entity, RoleDTO and RoleResponse
 * Uses MapStruct for automatic implementation
 */
@Mapper(componentModel = "spring", unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE, nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface RoleMapper {

    RoleDTO toDTO(Role role);

    Role toEntity(RoleDTO roleDTO);

    RoleResponse toResponse(RoleDTO roleDTO);

    List<RoleDTO> toDTOList(List<Role> roles);

    List<RoleResponse> toResponseList(List<RoleDTO> roleDTOs);
}
