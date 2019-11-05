package com.syscom.mapper.event;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.syscom.beans.User;
import com.syscom.event.user.UserUpsertEvent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserUpsertEventMapper {

	UserUpsertEvent beanToEvent(User user);

}
