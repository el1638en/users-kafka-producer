package com.syscom.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import com.syscom.beans.User;
import com.syscom.dto.UserDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {})
public interface UserMapper {

	@Mappings({})
	UserDTO beanToDto(User user);

	@InheritInverseConfiguration
	@Mappings({ @Mapping(target = "id", ignore = true) })
	User dtoToBean(UserDTO userDTO);

}
