package com.project.library.contracts;

import java.time.Instant;
import java.util.UUID;

public record EventMetadata(
    UUID eventId,
    String eventType,
    int schemaVersion,
    String producer,
    String aggregateType,
    UUID aggregateId,
    long aggregateVersion,
    Instant occurredAt,
    EventContext context
) {
    public EventMetadata {
        if (eventId == null || aggregateId == null || occurredAt == null || context == null || eventType == null || eventType.isBlank() || producer == null || producer.isBlank() || aggregateType == null || aggregateType.isBlank() || schemaVersion <= 0 || aggregateVersion < 0) {
            throw new IllegalArgumentException("Invalid field(s) for the event metadata");
        }
    }
}