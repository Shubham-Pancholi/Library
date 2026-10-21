package com.project.library.circulation.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.project.library.TestcontainersConfiguration;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (
    {
        TestcontainersConfiguration.class,
        EventInboxRepository.class
    }
)
public class EventInboxRepositoryTests {

    @Autowired 
    private EventInboxRepository repository;

    @Autowired 
    private JdbcTemplate jdbcTemplate;

    @Test 
    public void firstDelivery() {
        String consumerName = "Name";
        UUID eventId = UUID.randomUUID();
        Instant time = Instant.parse("2026-10-01T00:00:00Z");

        assertThat(repository.tryRecord(consumerName, eventId, time)).isTrue();

        var timestamp = jdbcTemplate.queryForObject("""
                SELECT processed_at FROM circulation.event_inbox
                WHERE consumer_name = ? AND event_id = ?
                """, java.sql.Timestamp.class, consumerName, eventId);

        assertThat(timestamp).isEqualTo(time);
    }

    @Test 
    public void duplicateDelivery() {
        String consumerName = "Name";
        UUID eventId = UUID.randomUUID();
        Instant time1 = Instant.parse("2026-10-01T00:00:00Z");
        Instant time2 = Instant.parse("2026-11-01T00:00:00Z");

        assertThat(repository.tryRecord(consumerName, eventId, time1)).isTrue();

        assertThat(repository.tryRecord(consumerName, eventId, time2)).isFalse();

        var timestamps = jdbcTemplate.queryForList("""
                SELECT processed_at FROM circulation.event_inbox
                WHERE consumer_name = ? AND event_id = ?
                """, java.sql.Timestamp.class, consumerName, eventId);

        assertThat(timestamps.size()).isEqualTo(1);
        var timestamp = timestamps.get(0);

        assertThat(timestamp.toInstant()).isEqualTo(time1);
    }

    @Test 
    public void multipleConsumers() {
        String consumerName = "Name";
        String consumerName2 = "Name2";
        UUID eventId = UUID.randomUUID();
        Instant time1 = Instant.parse("2026-10-01T00:00:00Z");
        Instant time2 = Instant.parse("2026-11-01T00:00:00Z");

        assertThat(repository.tryRecord(consumerName, eventId, time1)).isTrue();

        assertThat(repository.tryRecord(consumerName2, eventId, time2)).isTrue();

        var timestamps = jdbcTemplate.queryForList("""
                SELECT processed_at FROM circulation.event_inbox
                WHERE event_id = ?
                """, java.sql.Timestamp.class, eventId);

        assertThat(timestamps.size()).isEqualTo(2);
        var timestamp1 = timestamps.get(0);
        var timestamp2 = timestamps.get(1);

        assertThat(timestamp1).isNotEqualTo(timestamp2);
    }

    @Test 
    @Transactional (propagation = Propagation.NOT_SUPPORTED)
    public void eventInboxWithoutTransactionIsRejected() {
        String consumerName = "Name";
        UUID eventId = UUID.randomUUID();
        Instant time = Instant.parse("2026-10-01T00:00:00Z");

        try {
            assertThatThrownBy(() -> repository.tryRecord(consumerName, eventId, time))
                .isInstanceOf(IllegalTransactionStateException.class);

            var timestamps = jdbcTemplate.queryForList("""
                    SELECT processed_at FROM circulation.event_inbox
                    WHERE consumer_name = ? AND event_id = ?
                    """, java.sql.Timestamp.class, consumerName, eventId);

            assertThat(timestamps.size()).isEqualTo(0);
        }
        finally {
            jdbcTemplate.update("""
                    DELETE FROM circulation.event_inbox
                    WHERE consumer_name = ? AND event_id = ?
                    """, consumerName, eventId);
        }
    }

    @Test 
    public void rollbackTest() {
        String consumerName = "Name";
        UUID eventId = UUID.randomUUID();
        Instant time = Instant.parse("2026-10-01T00:00:00Z");

        assertThat(repository.tryRecord(consumerName, eventId, time)).isTrue();

        TestTransaction.flagForRollback();
        TestTransaction.end();

        var timestamps = jdbcTemplate.queryForList("""
                SELECT processed_at FROM circulation.event_inbox
                WHERE consumer_name = ? AND event_id = ?
                """, java.sql.Timestamp.class, consumerName, eventId);

        assertThat(timestamps.size()).isEqualTo(0);

        TestTransaction.start();

        assertThat(repository.tryRecord(consumerName, eventId, time)).isTrue();
    }
}