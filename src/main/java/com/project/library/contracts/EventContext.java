package com.project.library.contracts;

import java.util.UUID;

public record EventContext(
    UUID correlationId,
    UUID causationId,
    UUID importBatchId,
    boolean replay
) {
    public EventContext {
        if (correlationId == null) {
            throw new IllegalArgumentException("Null correlation id in the event context");
        }
    }
}