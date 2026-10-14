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

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (TestcontainersConfiguration.class)
public class MembershipPlanRepositoryTests {

    @Autowired 
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired 
    private MembershipPlanVersionRepository membershipPlanVersionRepository;

    @Autowired 
    private EntityManager entityManager;

    @Test 
    public void saveAndReloadPlanWithTwoVersion() {
        MembershipPlan plan = membershipPlanRepository.saveAndFlush(new MembershipPlan("plan"));

        assertThat(plan).isNotNull();
        assertThat(plan.getId()).isNotNull();
        assertThat(plan.getName()).isEqualTo("plan");

        UUID planId = plan.getId();
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = Instant.parse("2027-01-01T00:00:00Z");
        int maxCopies = 2;
        int maxDays = 14;

        MembershipPlanVersion version1 = membershipPlanVersionRepository.saveAndFlush(new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays));

        assertThat(version1).isNotNull();

        effectiveFrom = effectiveTo;
        effectiveTo = null;

        maxCopies = 3; maxDays = 21;
        MembershipPlanVersion version2 = membershipPlanVersionRepository.saveAndFlush(new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays));

        assertThat(version2).isNotNull();

        entityManager.clear();

        MembershipPlan loadedPlan = membershipPlanRepository.findById(planId)
                                    .orElseThrow();

        MembershipPlanVersion loadedVersion1 = membershipPlanVersionRepository.findById(version1.getId())
                                                .orElseThrow();

        MembershipPlanVersion loadedVersion2 = membershipPlanVersionRepository.findById(version2.getId())
                                                .orElseThrow();

        assertThat(loadedPlan.getId()).isEqualTo(planId);
        assertThat(loadedPlan.getName()).isEqualTo("plan");

        assertThat(loadedVersion1.getId()).isNotEqualTo(loadedVersion2.getId());

        assertThat(loadedVersion1.getPlanId()).isEqualTo(loadedVersion2.getPlanId()).isEqualTo(planId);

        assertThat(loadedVersion1.getEffectiveFrom()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(loadedVersion1.getEffectiveTo()).isEqualTo(effectiveFrom);

        assertThat(loadedVersion2.getEffectiveFrom()).isEqualTo(effectiveFrom);
        assertThat(loadedVersion2.getEffectiveTo()).isEqualTo(effectiveTo);

        assertThat(loadedVersion1.getMaxCopies()).isEqualTo(2);
        assertThat(loadedVersion2.getMaxCopies()).isEqualTo(3);

        assertThat(loadedVersion1.getMaxDays()).isEqualTo(14);
        assertThat(loadedVersion2.getMaxDays()).isEqualTo(21);
    }

    @Test 
    public void rejectVersionWithoutValidPlan() {
        UUID planId = UUID.randomUUID();
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = Instant.parse("2027-01-01T00:00:00Z");
        int maxCopies = 2;
        int maxDays = 14;

        MembershipPlanVersion version = new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays);

        assertThatThrownBy(() -> membershipPlanVersionRepository.saveAndFlush(version))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}