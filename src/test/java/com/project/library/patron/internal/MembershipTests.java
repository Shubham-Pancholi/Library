package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

public class MembershipTests {

    @ParameterizedTest 
    @EnumSource (MembershipStatus.class)
    public void retainsValidValues(MembershipStatus status) {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");

        Membership membership = new Membership(patronId, planVersionId, startsAt, endsAt, status);

        assertThat(membership).isNotNull();
        assertThat(membership.getPatronId()).isEqualTo(patronId);
        assertThat(membership.getPlanVersionId()).isEqualTo(planVersionId);
        assertThat(membership.getStartsAt()).isEqualTo(startsAt);
        assertThat(membership.getEndsAt()).isEqualTo(endsAt);
        assertThat(membership.getMembershipStatus()).isEqualTo(status);
    }

    @Test 
    public void rejectsNullPatronId() {
        UUID patronId = null;
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        assertThatThrownBy(() -> new Membership(patronId, planVersionId, startsAt, endsAt, status))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectsNullPlanVersionId() {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = null;
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        assertThatThrownBy(() -> new Membership(patronId, planVersionId, startsAt, endsAt, status))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectsNullStartDate() {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = null;
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        assertThatThrownBy(() -> new Membership(patronId, planVersionId, startsAt, endsAt, status))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectsNullEndDate() {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = null;
        MembershipStatus status = MembershipStatus.ACTIVE;

        assertThatThrownBy(() -> new Membership(patronId, planVersionId, startsAt, endsAt, status))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectsNullMembershipStatus() {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");
        MembershipStatus status = null;

        assertThatThrownBy(() -> new Membership(patronId, planVersionId, startsAt, endsAt, status))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void equalStartEndDateAreRejected() {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2026-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.CANCELLED;

        assertThatThrownBy(() -> new Membership(patronId, planVersionId, startsAt, endsAt, status))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void endDateBeforeStartDateIsRejected() {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2027-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2026-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.CANCELLED;

        assertThatThrownBy(() -> new Membership(patronId, planVersionId, startsAt, endsAt, status))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectsNullValidationInstant() {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        Membership membership = new Membership(patronId, planVersionId, startsAt, endsAt, status);

        assertThatThrownBy(() -> membership.isValidAt(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @ValueSource (strings = {"2026-01-01T00:00:00Z","2026-12-31T23:59:59Z"})
    public void validInstantForValidationReturnsTrue(String instantString) {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        Membership membership = new Membership(patronId, planVersionId, startsAt, endsAt, status);
        
        assertThat(membership.isValidAt(Instant.parse(instantString))).isEqualTo(true);
    }

    @ParameterizedTest 
    @ValueSource (strings = {"2025-12-31T23:59:59Z","2027-01-01T00:00:00Z", "2027-01-01T00:00:01Z"})
    public void invalidInstantForValidationReturnsFalse(String instantString) {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        Membership membership = new Membership(patronId, planVersionId, startsAt, endsAt, status);
        
        assertThat(membership.isValidAt(Instant.parse(instantString))).isEqualTo(false);
    }

    @Test 
    public void cancelledStatusForValidInstantReturnsFalse() {
        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2027-01-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.CANCELLED;

        Membership membership = new Membership(patronId, planVersionId, startsAt, endsAt, status);

        assertThat(membership.isValidAt(Instant.parse("2026-06-06T00:00:00Z"))).isEqualTo(false);
    }
}