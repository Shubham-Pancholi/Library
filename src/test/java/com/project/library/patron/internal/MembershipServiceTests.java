package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import com.project.library.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;

@SpringBootTest 
@Import (
    {
        TestcontainersConfiguration.class,
        MembershipServiceTests.Config.class
    }
)
@Transactional 
public class MembershipServiceTests {

    @TestConfiguration
    static class Config {
        
        @Bean
        @Primary 
        public Clock testClock() {
            return Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);
        }
    }

    private final MembershipService membershipService;
    private final MembershipRepository membershipRepository;
    private final MembershipPlanRepository membershipPlanRepository;
    private final MembershipPlanVersionRepository membershipPlanVersionRepository;
    private final PatronRepository patronRepository;
    private final EntityManager entityManager;

    @Autowired 
    public MembershipServiceTests(
        MembershipService aMembershipService,
        MembershipRepository aMembershipRepository,
        MembershipPlanRepository aMembershipPlanRepository,
        MembershipPlanVersionRepository aMembershipPlanVersionRepository,
        PatronRepository aPatronRepository,
        EntityManager aEntityManager
    ) {
        membershipService = aMembershipService;
        membershipRepository = aMembershipRepository;
        membershipPlanRepository = aMembershipPlanRepository;
        membershipPlanVersionRepository = aMembershipPlanVersionRepository;
        patronRepository = aPatronRepository;
        entityManager = aEntityManager;
    }

    @Test 
    @WithMockUser (roles = "STAFF")
    public void staffReadsAnActiveMembership() {

        Patron patron = patronRepository.saveAndFlush(new Patron(PatronType.MEMBER, "Name", null, null));
        MembershipPlan plan = membershipPlanRepository.saveAndFlush(new MembershipPlan("plan"));
        MembershipPlanVersion version = membershipPlanVersionRepository.saveAndFlush(new MembershipPlanVersion(plan.getId(), Instant.parse("2026-01-01T00:00:00Z"), null, 2, 14));
        Membership membership = membershipRepository.saveAndFlush(new Membership(patron.getId(), version.getId(), Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-08-10T16:00:00Z"), MembershipStatus.ACTIVE));

        entityManager.clear();

        MembershipDetails membershipDetails = membershipService.findMembership(membership.getId())
                                                .orElseThrow();

        assertThat(membershipDetails.id()).isEqualTo(membership.getId());
        assertThat(membershipDetails.patronId()).isEqualTo(membership.getPatronId());
        assertThat(membershipDetails.planVersionId()).isEqualTo(membership.getPlanVersionId());
        assertThat(membershipDetails.startsAt()).isEqualTo(membership.getStartsAt());
        assertThat(membershipDetails.endsAt()).isEqualTo(membership.getEndsAt());
        assertThat(membershipDetails.status()).isEqualTo(membership.getMembershipStatus());
        assertThat(membershipDetails.currentlyValid()).isEqualTo(true);
    }

    @Test 
    @WithMockUser (roles = "STAFF")
    public void staffReadsAnActiveMembershipEndingAtTheInstant() {
        Patron patron = patronRepository.saveAndFlush(new Patron(PatronType.MEMBER, "Name", null, null));
        MembershipPlan plan = membershipPlanRepository.saveAndFlush(new MembershipPlan("plan"));
        MembershipPlanVersion version = membershipPlanVersionRepository.saveAndFlush(new MembershipPlanVersion(plan.getId(), Instant.parse("2026-01-01T00:00:00Z"), null, 2, 14));
        Membership membership = membershipRepository.saveAndFlush(new Membership(patron.getId(), version.getId(), Instant.parse("2026-01-01T08:00:00Z"), Instant.parse("2026-06-15T12:00:00Z"), MembershipStatus.ACTIVE));

        entityManager.clear();

        MembershipDetails membershipDetails = membershipService.findMembership(membership.getId())
                                                .orElseThrow();

        assertThat(membershipDetails.currentlyValid()).isEqualTo(false);
    }

    @Test 
    @WithMockUser (roles = "STAFF")
    public void requestingUnknownMembershipIdReturnsEmpty() {
        Optional<MembershipDetails> membershipDetails = membershipService.findMembership(UUID.randomUUID());

        assertThat(membershipDetails).isEmpty();
    }

    @Test 
    @WithMockUser 
    public void nonStaffUserDeniedRequestingMembership() {
        assertThatThrownBy(() -> membershipService.findMembership(UUID.randomUUID()))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test 
    public void unauthenticatedMembershipRequestsAreDenied() {
        assertThatThrownBy(() -> membershipService.findMembership(UUID.randomUUID()))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}