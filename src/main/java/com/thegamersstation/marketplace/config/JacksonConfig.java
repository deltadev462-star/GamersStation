package com.thegamersstation.marketplace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Configures Jackson to serialize LocalDateTime with a 'Z' suffix so that
 * browser clients (new Date(...)) correctly interpret timestamps as UTC.
 *
 * Without this, LocalDateTime serializes as "2024-01-15T10:30:00" (no timezone),
 * and browsers treat it as local time, causing a 3-hour offset for Asia/Riyadh users.
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter UTC_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .serializers(new LocalDateTimeSerializer(UTC_FORMATTER))
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
