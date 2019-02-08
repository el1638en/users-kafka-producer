package com.syscom.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.syscom.event.UserEvent;

@Component
public class UserProducer {

	private final Logger logger = LoggerFactory.getLogger(UserProducer.class);
	
	@Autowired
	private KafkaTemplate<String, UserEvent> userKafkaTemplate;

	@Value(value = "${user.topic.name}")
	private String userTopicName;

	public void send(UserEvent userEvent) {
		logger.info("Envoi de l'event {} dans le topic kafka {}.", userEvent, userTopicName);
		userKafkaTemplate.send(userTopicName, userEvent);
	}

}
