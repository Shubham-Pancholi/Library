package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Propagation;

import com.project.library.TestcontainersConfiguration;
import com.project.library.contracts.MembershipTerms;
import com.project.library.contracts.PatronEligibilitySnapshot;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (
    {
        TestcontainersConfiguration.class,
        PatronEligibilitySnapshotFactory.class
    }
)
public class PatronEligibilitySnapshotFactoryTests {

    @Autowired 
    private PatronEligibilitySnapshotFactory factory;

    @Autowired 
    private MembershipRepository membershipRepository;

    @Autowired 
    private PatronRepository patronRepository;

    @Autowired 
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired 
    private MembershipPlanVersionRepository membershipPlanVersionRepository;

    @PersistenceContext 
    private EntityManager entityManager;

    @Test 
    public void blockedPatronRetainsMembershipTerms() {
        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);
        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        patron.blockAdministratively("Reason A");

        assertThat(patron.getEligibilityVersion()).isEqualTo(1);

        MembershipPlan plan = new MembershipPlan("Plan");
        UUID planId = membershipPlanRepository.saveAndFlush(plan).getId();

        MembershipPlanVersion version1 = new MembershipPlanVersion(planId, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-04-01T00:00:00Z"), 2, 14);
        UUID version1Id = membershipPlanVersionRepository.saveAndFlush(version1).getId();

        MembershipPlanVersion version2 = new MembershipPlanVersion(planId, Instant.parse("2026-04-01T00:00:01Z"), null, 5, 28);
        membershipPlanVersionRepository.saveAndFlush(version2).getId();

        Membership membership = new Membership(patronId, version1Id, Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-12-01T00:00:00Z"), MembershipStatus.ACTIVE);
        UUID membershipId = membershipRepository.saveAndFlush(membership).getId();

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findByIdForUpdate(patronId).orElseThrow();

        PatronEligibilitySnapshot snap = factory.create(reloadedPatron, Instant.parse("2026-05-15T12:00:00Z"));

        assertThat(snap.patronId()).isEqualTo(patronId);
        assertThat(snap.sourceVersion()).isEqualTo(1);
        assertThat(snap.administrativeBlock()).isTrue();
        assertThat(snap.membership()).isNotNull();
        MembershipTerms terms = snap.membership();
        assertThat(terms.membershipId()).isEqualTo(membershipId);
        assertThat(terms.planVersionId()).isEqualTo(version1Id);
        assertThat(terms.validFrom()).isEqualTo(Instant.parse("2026-03-01T00:00:00Z"));
        assertThat(terms.validUntil()).isEqualTo(Instant.parse("2026-12-01T00:00:00Z"));
        assertThat(terms.maxCopies()).isEqualTo(2);
        assertThat(terms.maxDays()).isEqualTo(14);
    }

    @Test 
    public void visitorNonmemberRetainsTerms() {
        Patron patron = new Patron(PatronType.VISITOR, "FName", null, null);
        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findByIdForUpdate(patronId).orElseThrow();

        PatronEligibilitySnapshot snap = factory.create(reloadedPatron, Instant.parse("2026-05-15T12:00:00Z"));

        assertThat(snap.patronId()).isEqualTo(patronId);
        assertThat(snap.sourceVersion()).isEqualTo(0);
        assertThat(snap.administrativeBlock()).isFalse();
        assertThat(snap.membership()).isNull();
    }

    @Test 
    public void twoActiveSimultaneousMembershipsThrows() {
        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);
        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        MembershipPlan plan = new MembershipPlan("Plan");
        UUID planId = membershipPlanRepository.saveAndFlush(plan).getId();

        MembershipPlanVersion version1 = new MembershipPlanVersion(planId, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-04-01T00:00:00Z"), 2, 14);
        UUID version1Id = membershipPlanVersionRepository.saveAndFlush(version1).getId();

        MembershipPlanVersion version2 = new MembershipPlanVersion(planId, Instant.parse("2026-04-01T00:00:01Z"), null, 5, 28);
        UUID version2Id = membershipPlanVersionRepository.saveAndFlush(version2).getId();

        Membership membership1 = new Membership(patronId, version1Id, Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-12-01T00:00:00Z"), MembershipStatus.ACTIVE);
        membershipRepository.saveAndFlush(membership1).getId();

        Membership membership2 = new Membership(patronId, version2Id, Instant.parse("2026-05-01T00:00:00Z"), Instant.parse("2027-01-01T00:00:00Z"), MembershipStatus.ACTIVE);
        membershipRepository.saveAndFlush(membership2).getId();

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findByIdForUpdate(patronId).orElseThrow();

        assertThatThrownBy(() -> factory.create(reloadedPatron, Instant.parse("2026-05-15T12:00:00Z")))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test 
    @Transactional (propagation = Propagation.NOT_SUPPORTED)
    public void test() {
        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);
        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        Instant at = Instant.parse("2026-10-20T20:00:00Z");

        try {
            assertThatThrownBy(() -> factory.create(patron, at))
                .isInstanceOf(IllegalTransactionStateException.class);
        }
        finally {
            patronRepository.deleteById(patronId);
        }
    }
}