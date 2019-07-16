package com.syscom.mapper.dto;

import static org.apache.commons.lang3.StringUtils.upperCase;

import org.mapstruct.AfterMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.syscom.beans.User;
import com.syscom.dto.UserDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {})
public abstract class UserMapper {

	public abstract UserDTO beanToDto(User user);

	@InheritInverseConfiguration
	@Mapping(target = "id", ignore = true)
	public abstract User dtoToBean(UserDTO userDTO);

	@AfterMapping
	protected void convertToUpperCase(@MappingTarget User user) {
		user.setLogin(upperCase(user.getLogin()));
		user.setName(upperCase(user.getName()));
		user.setFirstName(upperCase(user.getFirstName()));
	}

}
