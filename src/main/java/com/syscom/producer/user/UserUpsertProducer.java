package com.syscom.producer.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.syscom.event.user.UserUpsertEvent;
import com.syscom.producer.AbstractEventProducer;

@Component
public class UserUpsertProducer extends AbstractEventProducer<UserUpsertEvent> {

	public UserUpsertProducer(@Value(value = "${spring.kafka.producer.topic.user.upsert:users-upsert}") String topic) {
		super(topic);
	}

}
