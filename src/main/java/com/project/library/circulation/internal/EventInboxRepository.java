package com.project.library.circulation.internal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository 
public class EventInboxRepository {

    private final JdbcTemplate jdbcTemplate;

    public EventInboxRepository (JdbcTemplate aJdbcTemplate) {
        jdbcTemplate = aJdbcTemplate;
    }

    @Transactional (propagation = Propagation.MANDATORY)
    public boolean tryRecord(String consumerName, UUID eventId, Instant processedAt) {
        if (consumerName == null || consumerName.isBlank() || eventId == null || processedAt == null) {
            throw new IllegalArgumentException("Invalid field(s) when creating the event inbox record");
        }

        int rowsAffected = jdbcTemplate.update("""
                INSERT INTO circulation.event_inbox
                    (consumer_name, event_id, processed_at)
                VALUES (?, ?, ?)
                ON CONFLICT (consumer_name, event_id) DO NOTHING
                """, consumerName, eventId, java.sql.Timestamp.from(processedAt)
            );

        if (rowsAffected == 1) return true;
        return false;
    }
}