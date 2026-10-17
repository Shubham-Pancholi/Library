package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.project.library.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (TestcontainersConfiguration.class)
public class AdministrativeBlockHistoryRepositoryTests {

    @Autowired 
    private AdministrativeBlockHistoryRepository administrativeBlockHistoryRepository;

    @Autowired 
    private PatronRepository patronRepository;

    @PersistenceContext 
    private EntityManager entityManager;

    @Test 
    public void saveAndReloadTwoEntriesForAPatron() {
        Patron patron = new Patron(PatronType.MEMBER, "Asha", "Rao", "0011");

        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        entityManager.clear();

        AdministrativeBlockHistory history1 = new AdministrativeBlockHistory(
            patronId,
            AdministrativeBlockAction.BLOCKED,
            "Book not returned",
            Instant.parse("2026-10-17T10:30:00Z"),
            "http://com.project.library/realms/library",
            "Staff Id: 09"
        );

        UUID idEntry1 = administrativeBlockHistoryRepository.saveAndFlush(history1).getId();

        assertThat(idEntry1).isNotNull();

        entityManager.clear();

        AdministrativeBlockHistory history2 = new AdministrativeBlockHistory(
            patronId,
            AdministrativeBlockAction.CLEARED,
            "Book returned; fine paid",
            Instant.parse("2026-10-17T14:04:30Z"),
            "http://com.project.library/realms/library",
            "Staff Id: 08"
        );

        UUID idEntry2 = administrativeBlockHistoryRepository.saveAndFlush(history2).getId();

        assertThat(idEntry2).isNotNull().isNotEqualTo(idEntry1);

        entityManager.clear();

        AdministrativeBlockHistory loadedEntry1 = administrativeBlockHistoryRepository.findById(idEntry1)
                                                    .orElseThrow();

        AdministrativeBlockHistory loadedEntry2 = administrativeBlockHistoryRepository.findById(idEntry2)
                                                    .orElseThrow();

        assertThat(loadedEntry1).isNotNull();
        assertThat(loadedEntry2).isNotNull();

        assertThat(loadedEntry1.getId()).isNotNull().isEqualTo(idEntry1);
        assertThat(loadedEntry2.getId()).isNotNull().isEqualTo(idEntry2);

        assertThat(loadedEntry1.getPatronId()).isEqualTo(patronId);
        assertThat(loadedEntry2.getPatronId()).isEqualTo(patronId);

        assertThat(loadedEntry1.getAction()).isEqualTo(AdministrativeBlockAction.BLOCKED);
        assertThat(loadedEntry2.getAction()).isEqualTo(AdministrativeBlockAction.CLEARED);

        assertThat(loadedEntry1.getReason()).isEqualTo("Book not returned");
        assertThat(loadedEntry2.getReason()).isEqualTo("Book returned; fine paid");

        assertThat(loadedEntry1.getChangedAt()).isEqualTo(Instant.parse("2026-10-17T10:30:00Z"));
        assertThat(loadedEntry2.getChangedAt()).isEqualTo(Instant.parse("2026-10-17T14:04:30Z"));

        assertThat(loadedEntry1.getActorSubject()).isEqualTo("Staff Id: 09");
        assertThat(loadedEntry2.getActorSubject()).isEqualTo("Staff Id: 08");

        assertThat(loadedEntry1.getActorIssuer()).isEqualTo("http://com.project.library/realms/library");
        assertThat(loadedEntry2.getActorIssuer()).isEqualTo("http://com.project.library/realms/library");
    }

    @Test 
    public void blockingNonExistingUserIsNotAllowed() {
        UUID patron = UUID.randomUUID();

        AdministrativeBlockHistory history = new AdministrativeBlockHistory(
            patron,
            AdministrativeBlockAction.BLOCKED,
            "Book not returned",
            Instant.parse("2026-10-17T10:30:00Z"),
            "http://com.project.library/realms/library",
            "Staff Id: 09"
        );

        assertThatThrownBy(() -> administrativeBlockHistoryRepository.saveAndFlush(history))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}