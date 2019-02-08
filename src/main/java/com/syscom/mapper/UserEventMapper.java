package com.syscom.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import com.syscom.beans.User;
import com.syscom.event.UserEvent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserEventMapper {

	@Mappings({ })
	UserEvent beanToEvent(User user);

}
