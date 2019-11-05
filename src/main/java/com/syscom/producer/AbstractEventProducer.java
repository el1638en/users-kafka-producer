package com.syscom.producer;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

public abstract class AbstractEventProducer<T> {

	private final Logger logger = LoggerFactory.getLogger(AbstractEventProducer.class);

	private String topic;

	@Autowired
	private KafkaTemplate<String, T> kafkaTemplate;

	public AbstractEventProducer(String topic) {
		this.topic = topic;
	}

	public void send(T event) {
		doSend(null, event);
	}

	public void send(String key, T event) {
		doSend(key, event);
	}

	private void doSend(String key, T event) {
		LocalDateTime start = LocalDateTime.now();
		logger.info("Treatment of event {} {}, key {} in the kafka's topic {}.", event, event.getClass(), key, topic);
		ListenableFuture<SendResult<String, T>> future = kafkaTemplate.send(topic, key, event);
		future.addCallback(new ListenableFutureCallback<SendResult<String, T>>() {
			@Override
			public void onSuccess(final SendResult<String, T> sendResultMessage) {
				LocalDateTime end = LocalDateTime.now();
				int duration = Duration.between(start, end).getNano() / 1000000;
				logger.info("Sent Successffuly event {} {}, key {},  with offset {} (duration {} ms).", event,
						event.getClass(), key, sendResultMessage.getRecordMetadata().offset(), duration);
			}

			@Override
			public void onFailure(final Throwable throwable) {
				logger.error("unable to send event {}, key {}.", event, key, throwable);
			}
		});
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

}
