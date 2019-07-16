package com.syscom.mapper.dto;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.syscom.beans.Category;
import com.syscom.dto.CategoryDTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, uses = {})
public interface CategoryMapper {

	CategoryDTO beanToDto(Category category);

	@InheritInverseConfiguration
	@Mapping(target = "id", ignore = true)
	Category dtoToBean(CategoryDTO categoryDTO);

}
