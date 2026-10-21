package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.project.library.TestcontainersConfiguration;
import com.project.library.contracts.EventContext;
import com.project.library.contracts.PatronEligibilityChanged;
import com.project.library.contracts.PatronEligibilitySnapshot;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (
    {
        TestcontainersConfiguration.class,
        PatronEligibilitySnapshotFactory.class,
        PatronEligibilityPublisher.class
    }
)
@RecordApplicationEvents 
public class PatronEligibilityPublisherTests {

    @Autowired 
    private PatronEligibilityPublisher publisher;

    @Autowired 
    private PatronRepository patronRepository;

    @Autowired 
    private ApplicationEvents events;

    @PersistenceContext 
    private EntityManager entityManager;

    @Test 
    public void successfulPublicationTest() {
        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);
        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        patron.blockAdministratively("Reason A");

        entityManager.flush();
        entityManager.clear();

        Patron rePatron = patronRepository.findByIdForUpdate(patronId).orElseThrow();

        EventContext context = new EventContext(UUID.randomUUID(), null, null, false);
        Instant time = Instant.parse("2026-10-20T21:00:00Z");

        publisher.publish(rePatron, time, context);

        var published = events.stream(PatronEligibilityChanged.class).toList();

        assertThat(published.size()).isEqualTo(1);
        assertThat(published.get(0).snapshot()).isEqualTo(
            new PatronEligibilitySnapshot(patronId, 1, true, null)
        );
        assertThat(published.get(0).metadata().aggregateId()).isEqualTo(patronId);
        assertThat(published.get(0).metadata().aggregateVersion()).isEqualTo(1);
        assertThat(published.get(0).metadata().occurredAt()).isEqualTo(time);
        assertThat(published.get(0).metadata().context()).isEqualTo(context);
    }
}