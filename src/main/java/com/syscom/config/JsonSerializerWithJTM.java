package com.syscom.config;

import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonSerializerWithJTM<T> extends JsonSerializer<T> {
    public JsonSerializerWithJTM() {
        super();
        objectMapper.registerModule(new JavaTimeModule());
    }
}