package com.syscom.producer.category;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.syscom.event.category.CategoryUpsertEvent;
import com.syscom.producer.AbstractEventProducer;

@Component
public class CategoryUpsertProducer extends AbstractEventProducer<CategoryUpsertEvent> {

	public CategoryUpsertProducer(
			@Value(value = "${spring.kafka.producer.topic.category.upsert:category-upsert}") String topic) {
		super(topic);
	}

}
