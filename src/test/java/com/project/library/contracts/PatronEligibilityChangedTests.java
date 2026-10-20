package com.project.library.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PatronEligibilityChangedTests {

    @Test 
    public void validValuesAreRetained() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;
        boolean block = true;
        MembershipTerms membership = null;

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = patron;
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        EventMetadata metadata = new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context);

        PatronEligibilityChanged change = new PatronEligibilityChanged(metadata, snapshot);

        assertThat(change.metadata()).isEqualTo(metadata);
        assertThat(change.snapshot()).isEqualTo(snapshot);
    }

    @Test 
    public void nullMetadataThrows() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;
        boolean block = true;
        MembershipTerms membership = null;

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

        assertThatThrownBy(() -> new PatronEligibilityChanged(null, snapshot))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullSnapshotThrows() {
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

        assertThatThrownBy(() -> new PatronEligibilityChanged(metadata, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void differentPatronsAreRejected() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;
        boolean block = true;
        MembershipTerms membership = null;

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

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

        assertThatThrownBy(() -> new PatronEligibilityChanged(metadata, snapshot))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @ValueSource (longs = {4, 6})
    public void differentVersionsAreRejected(long aggregateVersion) {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 5;
        boolean block = true;
        MembershipTerms membership = null;

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = patron;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        EventMetadata metadata = new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context);

        assertThatThrownBy(() -> new PatronEligibilityChanged(metadata, snapshot))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void invalidEventTypeThrows() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;
        boolean block = true;
        MembershipTerms membership = null;

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

        UUID eventId = UUID.randomUUID();
        String eventType = "BookSnapshotChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = patron;
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        EventMetadata metadata = new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context);

        assertThatThrownBy(() -> new PatronEligibilityChanged(metadata, snapshot))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void invalidMetadataProducerThrows() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;
        boolean block = true;
        MembershipTerms membership = null;

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "billing";
        String aggregateType = "Patron";
        UUID aggregateId = patron;
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        EventMetadata metadata = new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context);

        assertThatThrownBy(() -> new PatronEligibilityChanged(metadata, snapshot))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void invalidMetadaAggregateTypeThrows() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;
        boolean block = true;
        MembershipTerms membership = null;

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Membership";
        UUID aggregateId = patron;
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        EventMetadata metadata = new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context);

        assertThatThrownBy(() -> new PatronEligibilityChanged(metadata, snapshot))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void invalidMetadataSchemaVersionThrows() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;
        boolean block = true;
        MembershipTerms membership = null;

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 2;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = patron;
        long aggregateVersion = 0;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);

        EventMetadata metadata = new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context);

        assertThatThrownBy(() -> new PatronEligibilityChanged(metadata, snapshot))
            .isInstanceOf(IllegalArgumentException.class);
    }
}