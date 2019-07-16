package com.syscom.mapper.event;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.syscom.beans.Category;
import com.syscom.event.category.CategoryUpsertEvent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoryUpsertEventMapper {

	CategoryUpsertEvent beanToEvent(Category category);

}
