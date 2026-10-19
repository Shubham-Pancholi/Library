package com.project.library.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class EventMetadataTests {

    @Test 
    public void validValuesAreRetained() {
        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        EventMetadata metadata = new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context);

        assertThat(metadata.eventId()).isEqualTo(eventId);
        assertThat(metadata.eventType()).isEqualTo(eventType);
        assertThat(metadata.schemaVersion()).isEqualTo(schemaVersion);
        assertThat(metadata.producer()).isEqualTo(producer);
        assertThat(metadata.aggregateType()).isEqualTo(aggregateType);
        assertThat(metadata.aggregateId()).isEqualTo(aggregateId);
        assertThat(metadata.aggregateVersion()).isEqualTo(aggregateVersion);
        assertThat(metadata.occurredAt()).isEqualTo(occurredAt);
        assertThat(metadata.context()).isEqualTo(context);
    }

    @ParameterizedTest 
    @ValueSource (ints = {0, -1})
    public void invalidSchemaVersionIsRejected(int schemaVersion) {
        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void invalidAggregateVersionIsRejected() {
        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = -1;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullEventIdThrows() {
        UUID eventId = null;
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullAggregateIdThrows() {
        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = null;
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullOccurredAtThrows() {
        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = 0;
        Instant occurredAt = null;
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullContextThrows() {
        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = null;

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @NullAndEmptySource 
    @ValueSource (strings = {" ", "\t\n"})
    public void invalidEventTypeThrows(String eventType) {
        UUID eventId = UUID.randomUUID();
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @NullAndEmptySource 
    @ValueSource (strings = {" ", "\t\n"})
    public void invalidProducerThrows(String producer) {
        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String aggregateType = "Patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @NullAndEmptySource 
    @ValueSource (strings = {" ", "\t\n"})
    public void invalidAggregateTypeThrows(String aggregateType) {
        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        UUID aggregateId = UUID.randomUUID();
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        assertThatThrownBy(() -> new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context))
            .isInstanceOf(IllegalArgumentException.class);
    }
}