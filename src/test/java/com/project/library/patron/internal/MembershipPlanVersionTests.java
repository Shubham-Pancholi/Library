package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MembershipPlanVersionTests {

    @Test 
    public void retainsValidBorrowingTerms() {
        UUID planId = UUID.randomUUID();
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = Instant.parse("2027-01-01T00:00:00Z");
        int maxCopies = 2;
        int maxDays = 14;

        MembershipPlanVersion mpv = new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays);

        assertThat(mpv.getPlanId()).isEqualTo(planId);
        assertThat(mpv.getEffectiveFrom()).isEqualTo(effectiveFrom);
        assertThat(mpv.getEffectiveTo()).isEqualTo(effectiveTo);
        assertThat(mpv.getMaxCopies()).isEqualTo(maxCopies);
        assertThat(mpv.getMaxDays()).isEqualTo(maxDays);
    }

    @Test 
    public void allowsNoEffectiveTo() {
        UUID planId = UUID.randomUUID();
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = null;
        int maxCopies = 2;
        int maxDays = 14;

        MembershipPlanVersion mpv = new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays);

        assertThat(mpv).isNotNull();
        assertThat(mpv.getEffectiveTo()).isNull();
    }

    @Test 
    public void rejectsMissingEffectiveFrom() {
        UUID planId = UUID.randomUUID();
        Instant effectiveFrom = null;
        Instant effectiveTo = Instant.parse("2027-01-01T00:00:00Z");
        int maxCopies = 2;
        int maxDays = 14;

        assertThatThrownBy(() -> new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectsFromEqualtoTo() {
        UUID planId = UUID.randomUUID();
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = Instant.parse("2026-01-01T00:00:00Z");
        int maxCopies = 2;
        int maxDays = 14;

        assertThatThrownBy(() -> new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectsToBeforeFrom() {
        UUID planId = UUID.randomUUID();
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = effectiveFrom.minusSeconds(1);
        int maxCopies = 2;
        int maxDays = 14;

        assertThatThrownBy(() -> new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectsNullPlanId() {
        UUID planId = null;
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = effectiveFrom.plusSeconds(1);
        int maxCopies = 2;
        int maxDays = 14;

        assertThatThrownBy(() -> new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @ValueSource (ints = {0, -1, -2147483648})
    public void rejectsNonPositiveMaxCopies(int maxCopies) {
        UUID planId = UUID.randomUUID();
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = Instant.parse("2027-01-01T00:00:00Z");
        int maxDays = 14;

        assertThatThrownBy(() -> new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @ValueSource (ints = {0, -1, -2147483648})
    public void rejectsNonPositiveMaxDays(int maxDays) {
        UUID planId = UUID.randomUUID();
        Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant effectiveTo = Instant.parse("2027-01-01T00:00:00Z");
        int maxCopies = 2;

        assertThatThrownBy(() -> new MembershipPlanVersion(planId, effectiveFrom, effectiveTo, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }
}