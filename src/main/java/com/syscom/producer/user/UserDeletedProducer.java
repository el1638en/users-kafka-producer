package com.syscom.producer.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.syscom.event.user.UserDeletedEvent;
import com.syscom.producer.AbstractEventProducer;

@Component
public class UserDeletedProducer extends AbstractEventProducer<UserDeletedEvent> {

	public UserDeletedProducer(
			@Value(value = "${spring.kafka.producer.topic.user.deleted:users-deleted}") String topic) {
		super(topic);
	}
}
