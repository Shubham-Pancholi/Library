package com.project.library.patron.internal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.project.library.contracts.EventContext;
import com.project.library.contracts.EventMetadata;
import com.project.library.contracts.PatronEligibilityChanged;
import com.project.library.contracts.PatronEligibilitySnapshot;

@Component 
public class PatronEligibilityPublisher {

    private final PatronEligibilitySnapshotFactory factory;
    private final ApplicationEventPublisher eventPublisher;

    public PatronEligibilityPublisher (PatronEligibilitySnapshotFactory aFactory, ApplicationEventPublisher aEventPublisher) {
        factory = aFactory;
        eventPublisher = aEventPublisher;
    }

    @Transactional (propagation = Propagation.MANDATORY)
    public void publish(Patron patron, Instant occurredAt, EventContext context) {
        if (context == null) throw new IllegalArgumentException("Invalid Context when publishing Patron eligibility");

        PatronEligibilitySnapshot snap = factory.create(patron, occurredAt);

        EventMetadata metadata = new EventMetadata(
            UUID.randomUUID(),
            "PatronEligibilityChanged",
            1,
            "patron",
            "Patron",
            snap.patronId(),
            snap.sourceVersion(),
            occurredAt,
            context
        );

        PatronEligibilityChanged pec = new PatronEligibilityChanged(metadata, snap);

        eventPublisher.publishEvent(pec);
    }
}