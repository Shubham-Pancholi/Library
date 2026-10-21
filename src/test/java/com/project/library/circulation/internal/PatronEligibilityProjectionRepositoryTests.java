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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.project.library.TestcontainersConfiguration;
import com.project.library.contracts.MembershipTerms;
import com.project.library.contracts.PatronEligibilitySnapshot;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (TestcontainersConfiguration.class)
public class PatronEligibilityProjectionRepositoryTests {

    @Autowired 
    private PatronEligibilityProjectionRepository repository;

    @Autowired 
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext 
    private EntityManager entityManager;

    @Test 
    public void saveAndReloadSnapshotWithMembership() {
        UUID patron = UUID.randomUUID();
        MembershipTerms membershipTerms = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-06-01T12:00:00Z"), Instant.parse("2026-12-01T11:59:59Z"), 3, 21);
        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, 5, true, membershipTerms);

        repository.saveAndFlush(new PatronEligibilityProjection(snapshot));

        entityManager.clear();

        var pep = repository.findById(patron).orElseThrow();

        assertThat(pep.getPatronId()).isEqualTo(patron);
        assertThat(pep.getSourceVersion()).isEqualTo(5);
        assertThat(pep.getAdministrativeBlock()).isTrue();
        assertThat(pep.getMembershipId()).isEqualTo(membershipTerms.membershipId());
        assertThat(pep.getPlanVersionId()).isEqualTo(membershipTerms.planVersionId());
        assertThat(pep.getValidFrom()).isEqualTo(membershipTerms.validFrom());
        assertThat(pep.getValidUntil()).isEqualTo(membershipTerms.validUntil());
        assertThat(pep.getMaxCopies()).isEqualTo(3);
        assertThat(pep.getMaxDays()).isEqualTo(21);
    }

    @Test 
    public void saveAndReloadSnapshotWithoutMembership() {
        UUID patron = UUID.randomUUID();
        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, 0, false, null);

        repository.saveAndFlush(new PatronEligibilityProjection(snapshot));

        entityManager.clear();

        var pep = repository.findById(patron).orElseThrow();

        assertThat(pep.getPatronId()).isEqualTo(patron);
        assertThat(pep.getSourceVersion()).isEqualTo(0);
        assertThat(pep.getAdministrativeBlock()).isFalse();
        assertThat(pep.getMembershipId()).isNull();
        assertThat(pep.getPlanVersionId()).isNull();
        assertThat(pep.getValidFrom()).isNull();
        assertThat(pep.getValidUntil()).isNull();
        assertThat(pep.getMaxCopies()).isNull();
        assertThat(pep.getMaxDays()).isNull();
    }

    @Test 
    public void incompleteMembershipInSnapshotThrows() {
        UUID patron = UUID.randomUUID();
        MembershipTerms membershipTerms = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-06-01T12:00:00Z"), Instant.parse("2026-12-01T11:59:59Z"), 3, 21);
        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(patron, 5, true, membershipTerms);

        repository.saveAndFlush(new PatronEligibilityProjection(snapshot));

        assertThatThrownBy(() ->
            jdbcTemplate.update("""
                    UPDATE circulation.patron_eligibility_projection
                    SET max_days = NULL
                    WHERE patron_id = ?
                    """, patron))
            .isInstanceOf(DataIntegrityViolationException.class)
            .hasMessageContaining("validity_membership");
    }
}