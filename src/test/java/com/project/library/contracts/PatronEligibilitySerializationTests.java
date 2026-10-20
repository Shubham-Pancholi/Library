package com.project.library.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.events.core.EventSerializer;

import com.project.library.TestcontainersConfiguration;

@SpringBootTest 
@Import (TestcontainersConfiguration.class)
public class PatronEligibilitySerializationTests {

    @Autowired 
    private EventSerializer eventSerializer;

    @Test 
    public void validEventisSerializedAndDeserialized() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 5;
        boolean block = true;

        UUID membershipId = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        MembershipTerms membership = new MembershipTerms(membershipId, version, validFrom, validUntil, maxCopies, maxDays);

        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, sourceVersion, block, membership);

        UUID eventId = UUID.randomUUID();
        String eventType = "PatronEligibilityChanged";
        int schemaVersion = 1;
        String producer = "patron";
        String aggregateType = "Patron";
        UUID aggregateId = patron;
        long aggregateVersion = 5;
        Instant occurredAt = Instant.parse("2026-10-10T10:10:10Z");
        EventContext context = new EventContext(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), true);

        EventMetadata metadata = new EventMetadata(eventId, eventType, schemaVersion, producer, aggregateType, aggregateId, aggregateVersion, occurredAt, context);

        PatronEligibilityChanged event = new PatronEligibilityChanged(metadata, snapshot);

        Object serialized = eventSerializer.serialize(event);

        PatronEligibilityChanged restored = eventSerializer.deserialize(serialized, PatronEligibilityChanged.class);

        assertThat(restored).isNotNull();
        assertThat(restored).isEqualTo(event);
    }

    @Test 
    public void validOptionalEventisSerializedAndDeserialized() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;
        boolean block = false;
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

        PatronEligibilityChanged event = new PatronEligibilityChanged(metadata, snapshot);

        Object serialized = eventSerializer.serialize(event);

        PatronEligibilityChanged restored = eventSerializer.deserialize(serialized, PatronEligibilityChanged.class);

        assertThat(restored).isNotNull();
        assertThat(restored).isEqualTo(event);
    }
}