package com.project.library.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class EventContextTests {

    @Test 
    public void ordinaryActivity() {
        UUID correlationId = UUID.randomUUID();
        UUID causationId = null;
        UUID importBatchId = null;
        boolean replay = false;

        EventContext eventContext = new EventContext(correlationId, causationId, importBatchId, replay);

        assertThat(eventContext.correlationId()).isEqualTo(correlationId);
        assertThat(eventContext.causationId()).isNull();
        assertThat(eventContext.importBatchId()).isNull();
        assertThat(eventContext.replay()).isEqualTo(replay);
    }

    @Test 
    public void replayOfImportedActivity() {
        UUID correlationId = UUID.randomUUID();
        UUID causationId = UUID.randomUUID();
        UUID importBatchId = UUID.randomUUID();
        boolean replay = true;

        EventContext eventContext = new EventContext(correlationId, causationId, importBatchId, replay);

        assertThat(eventContext.correlationId()).isEqualTo(correlationId);
        assertThat(eventContext.causationId()).isEqualTo(causationId);
        assertThat(eventContext.importBatchId()).isEqualTo(importBatchId);
        assertThat(eventContext.replay()).isEqualTo(replay);
    }

    @Test 
    public void missingCorrelationIdThrows() {
        UUID correlationId = null;
        UUID causationId = UUID.randomUUID();
        UUID importBatchId = UUID.randomUUID();
        boolean replay = true;

        assertThatThrownBy(() -> new EventContext(correlationId, causationId, importBatchId, replay))
        .isInstanceOf(IllegalArgumentException.class);
    }
}