package com.project.library.contracts;

import java.util.Objects;

public record PatronEligibilityChanged(
    EventMetadata metadata,
    PatronEligibilitySnapshot snapshot
) {
    public PatronEligibilityChanged {
        if (metadata == null || snapshot == null) {
            throw new IllegalArgumentException("Invalid Field(s) for Patron eligibility changed");
        }

        if ( !("PatronEligibilityChanged".equals(metadata.eventType()) && metadata.schemaVersion() == 1 && "patron".equals(metadata.producer()) && "Patron".equals(metadata.aggregateType()) && Objects.equals(metadata.aggregateId(), snapshot.patronId()) && metadata.aggregateVersion() == snapshot.sourceVersion()) ) {
            throw new IllegalArgumentException("Invalid metadata or snapshot fields for Patron eligibility changed");
        }
    }
}