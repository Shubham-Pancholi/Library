package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class MembershipSelectorTests {

    private Instant at = Instant.parse("2026-06-15T12:00:00Z");

    @Test 
    public void noValidMembershipIsSupplied() {
        List<Membership> memberships = new ArrayList<>();

        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2026-06-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        Membership membership = new Membership(patronId, planVersionId, startsAt, endsAt, status);
        Membership membership2 = new Membership(patronId, planVersionId, startsAt, at.plusSeconds(1000), MembershipStatus.CANCELLED);
        Membership membership3 = new Membership(patronId, planVersionId, at.plusSeconds(10), at.plusSeconds(100000), status);

        memberships.add(membership);
        memberships.add(membership2);
        memberships.add(membership3);

        Optional<Membership> currentValid = MembershipSelector.selectCurrent(memberships, at);

        assertThat(currentValid).isEmpty();
    }

    @Test 
    public void oneValidMembershipIsSupplied() {
        List<Membership> memberships = new ArrayList<>();

        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2026-10-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        Membership membership1 = new Membership(patronId, planVersionId, startsAt, endsAt, status);
        Membership membership2 = new Membership(patronId, planVersionId, startsAt, at.minusSeconds(10), status);
        Membership membership3 = new Membership(patronId, planVersionId, at.plusSeconds(10), endsAt, status);
        Membership membership4 = new Membership(patronId, planVersionId, at.minusSeconds(10000), endsAt, MembershipStatus.CANCELLED);

        memberships.add(membership2);
        memberships.add(membership1);
        memberships.add(membership3);
        memberships.add(membership4);

        Optional<Membership> currentValid = MembershipSelector.selectCurrent(memberships, at);

        assertThat(currentValid).isNotEmpty();
        assertThat(currentValid.get()).isEqualTo(membership1);
    }

    @Test 
    public void moreThanOneValidMembershipThrows() {
        List<Membership> memberships = new ArrayList<>();

        UUID patronId = UUID.randomUUID();
        UUID planVersionId = UUID.randomUUID();
        Instant startsAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant endsAt = Instant.parse("2026-10-01T00:00:00Z");
        MembershipStatus status = MembershipStatus.ACTIVE;

        Membership membership1 = new Membership(patronId, planVersionId, startsAt, endsAt, status);
        Membership membership2 = new Membership(patronId, planVersionId, startsAt, at.minusSeconds(10), status);
        Membership membership3 = new Membership(patronId, planVersionId, at.minusSeconds(20), endsAt, status);

        memberships.add(membership1);
        memberships.add(membership2);
        memberships.add(membership3);

        assertThatThrownBy(() -> MembershipSelector.selectCurrent(memberships, at))
            .isInstanceOf(IllegalStateException.class);
    }
}