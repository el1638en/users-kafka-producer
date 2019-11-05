package com.syscom.producer.category;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.syscom.event.category.CategoryDeletedEvent;
import com.syscom.producer.AbstractEventProducer;

@Component
public class CategoryDeletedProducer extends AbstractEventProducer<CategoryDeletedEvent> {

	public CategoryDeletedProducer(
			@Value(value = "${spring.kafka.producer.topic.category.deleted:category-deleted}") String topic) {
		super(topic);
	}

}
