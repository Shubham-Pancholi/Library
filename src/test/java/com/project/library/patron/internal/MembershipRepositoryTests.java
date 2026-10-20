package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
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
public class MembershipRepositoryTests {

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
    public void saveAndReloadTwoMembershipsForPatron() {
        Patron patron = patronRepository.saveAndFlush(new Patron(PatronType.MEMBER, "Name", null, null));

        MembershipPlan plan = membershipPlanRepository.saveAndFlush(new MembershipPlan("plan"));

        MembershipPlanVersion version = membershipPlanVersionRepository.saveAndFlush(new MembershipPlanVersion(plan.getId(), Instant.parse("2026-01-01T00:00:00Z"), null, 2, 14));

        Membership membership1 = new Membership(patron.getId(), version.getId(), Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-10T16:00:00Z"), MembershipStatus.CANCELLED);

        Membership savedMembership1 = membershipRepository.saveAndFlush(membership1);

        Membership membership2 = new Membership(patron.getId(), version.getId(), Instant.parse("2026-06-01T09:00:00Z"), Instant.parse("2026-12-10T09:00:00Z"), MembershipStatus.ACTIVE);

        Membership savedMembership2 = membershipRepository.saveAndFlush(membership2);

        assertThat(savedMembership1).isNotNull();
        assertThat(savedMembership2).isNotNull();

        assertThat(savedMembership1.getId()).isNotNull();
        assertThat(savedMembership2.getId()).isNotNull();

        assertThat(savedMembership1.getId()).isNotEqualTo(savedMembership2.getId());

        assertThat(savedMembership1.getPatronId()).isEqualTo(savedMembership2.getPatronId()).isEqualTo(patron.getId());
        assertThat(savedMembership1.getPlanVersionId()).isEqualTo(savedMembership2.getPlanVersionId()).isEqualTo(version.getId());
        assertThat(savedMembership1.getStartsAt()).isEqualTo(Instant.parse("2026-01-01T08:00:00Z"));
        assertThat(savedMembership2.getStartsAt()).isEqualTo(Instant.parse("2026-06-01T09:00:00Z"));

        assertThat(savedMembership1.getEndsAt()).isEqualTo(Instant.parse("2026-01-10T16:00:00Z"));
        assertThat(savedMembership2.getEndsAt()).isEqualTo(Instant.parse("2026-12-10T09:00:00Z"));

        assertThat(savedMembership1.getMembershipStatus()).isEqualTo(MembershipStatus.CANCELLED);
        assertThat(savedMembership2.getMembershipStatus()).isEqualTo(MembershipStatus.ACTIVE);

        entityManager.clear();

        Membership loadedMembership1 = membershipRepository.findById(savedMembership1.getId())
                                        .orElseThrow();

        Membership loadedMembership2 = membershipRepository.findById(savedMembership2.getId())
                                        .orElseThrow();

        assertThat(loadedMembership1).isNotNull();
        assertThat(loadedMembership2).isNotNull();

        assertThat(loadedMembership1.getId()).isNotNull();
        assertThat(loadedMembership2.getId()).isNotNull();

        assertThat(loadedMembership1.getId()).isNotEqualTo(loadedMembership2.getId());

        assertThat(loadedMembership1.getPatronId()).isEqualTo(loadedMembership2.getPatronId()).isEqualTo(patron.getId());
        assertThat(loadedMembership1.getPlanVersionId()).isEqualTo(loadedMembership2.getPlanVersionId()).isEqualTo(version.getId());
        assertThat(loadedMembership1.getStartsAt()).isEqualTo(Instant.parse("2026-01-01T08:00:00Z"));
        assertThat(loadedMembership2.getStartsAt()).isEqualTo(Instant.parse("2026-06-01T09:00:00Z"));

        assertThat(loadedMembership1.getEndsAt()).isEqualTo(Instant.parse("2026-01-10T16:00:00Z"));
        assertThat(loadedMembership2.getEndsAt()).isEqualTo(Instant.parse("2026-12-10T09:00:00Z"));

        assertThat(loadedMembership1.getMembershipStatus()).isEqualTo(MembershipStatus.CANCELLED);
        assertThat(loadedMembership2.getMembershipStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test 
    public void rejectsNonExistingPatron() {
        UUID patron = UUID.randomUUID();

        MembershipPlan plan = membershipPlanRepository.saveAndFlush(new MembershipPlan("plan"));

        MembershipPlanVersion version = membershipPlanVersionRepository.saveAndFlush(new MembershipPlanVersion(plan.getId(), Instant.parse("2026-01-01T00:00:00Z"), null, 2, 14));

        Membership membership = new Membership(patron, version.getId(), Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-10T16:00:00Z"), MembershipStatus.CANCELLED);

        assertThatThrownBy(() -> membershipRepository.saveAndFlush(membership))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test 
    public void rejectsNonExistingPlanVersion() {
        Patron patron = patronRepository.saveAndFlush(new Patron(PatronType.MEMBER, "Name", null, null));

        UUID version = UUID.randomUUID();

        Membership membership = new Membership(patron.getId(), version, Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-10T16:00:00Z"), MembershipStatus.CANCELLED);

        assertThatThrownBy(() -> membershipRepository.saveAndFlush(membership))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test 
    public void loadingAllRecordsOfPatron() {
        Patron patronA = patronRepository.saveAndFlush(new Patron(PatronType.MEMBER, "Name", null, null));
        Patron patronB = patronRepository.saveAndFlush(new Patron(PatronType.MEMBER, "Guest", null, null));

        MembershipPlan plan = membershipPlanRepository.saveAndFlush(new MembershipPlan("plan"));
        MembershipPlanVersion version = membershipPlanVersionRepository.saveAndFlush(new MembershipPlanVersion(plan.getId(), Instant.parse("2026-01-01T00:00:00Z"), null, 2, 14));

        Membership membershipA1 = new Membership(patronA.getId(), version.getId(), Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-10T16:00:00Z"), MembershipStatus.CANCELLED);
        Membership membershipA2 = new Membership(patronA.getId(), version.getId(), Instant.parse("2026-06-01T09:00:00Z"), Instant.parse("2026-12-10T09:00:00Z"), MembershipStatus.ACTIVE);

        membershipRepository.saveAndFlush(membershipA1);
        membershipRepository.saveAndFlush(membershipA2);

        Membership membershipB1 = new Membership(patronB.getId(), version.getId(), Instant.parse("2026-05-10T16:00:00Z"), Instant.parse("2026-11-10T16:00:00Z"), MembershipStatus.ACTIVE);

        membershipRepository.saveAndFlush(membershipB1);

        entityManager.flush();
        entityManager.clear();

        List<Membership> recordsOfPatronA = membershipRepository.findByPatronId(patronA.getId());

        assertThat(recordsOfPatronA.size()).isEqualTo(2);
        assertThat(
            recordsOfPatronA.stream().filter(r -> r.getPatronId().equals(patronA.getId())).count()
        ).isEqualTo(2);
        assertThat(
            recordsOfPatronA.stream().filter(r -> r.getMembershipStatus().equals(MembershipStatus.ACTIVE)).count()
        ).isEqualTo(1);
    }

    @Test 
    public void loadingRecordsOfPatronWithoutMembershipReturnsEmpty() {
        Patron patronA = patronRepository.saveAndFlush(new Patron(PatronType.MEMBER, "Name", null, null));
        Patron patronB = patronRepository.saveAndFlush(new Patron(PatronType.VISITOR, "Guest", null, null));

        MembershipPlan plan = membershipPlanRepository.saveAndFlush(new MembershipPlan("plan"));
        MembershipPlanVersion version = membershipPlanVersionRepository.saveAndFlush(new MembershipPlanVersion(plan.getId(), Instant.parse("2026-01-01T00:00:00Z"), null, 2, 14));

        Membership membershipA1 = new Membership(patronA.getId(), version.getId(), Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-01-10T16:00:00Z"), MembershipStatus.CANCELLED);
        Membership membershipA2 = new Membership(patronA.getId(), version.getId(), Instant.parse("2026-06-01T09:00:00Z"), Instant.parse("2026-12-10T09:00:00Z"), MembershipStatus.ACTIVE);

        membershipRepository.saveAndFlush(membershipA1);
        membershipRepository.saveAndFlush(membershipA2);

        entityManager.flush();
        entityManager.clear();

        List<Membership> recordsOfPatronB = membershipRepository.findByPatronId(patronB.getId());

        assertThat(recordsOfPatronB.size()).isEqualTo(0);
    }
}