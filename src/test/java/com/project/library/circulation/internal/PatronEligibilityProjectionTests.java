package com.project.library.circulation.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.project.library.contracts.MembershipTerms;
import com.project.library.contracts.PatronEligibilitySnapshot;

public class PatronEligibilityProjectionTests {

    @Test 
    public void testingUpdateRetains() {
        MembershipTerms membershipTerms = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-10-01T00:00:00Z"), 2, 14);
        PatronEligibilitySnapshot snapshot = new PatronEligibilitySnapshot(UUID.randomUUID(), 5, false, membershipTerms);
        PatronEligibilityProjection projection = new PatronEligibilityProjection(snapshot);

        PatronEligibilitySnapshot snapshot2 = new PatronEligibilitySnapshot(snapshot.patronId(), 8, true, null);

        assertThat(projection.applyIfNewer(snapshot2)).isTrue();

        assertThat(projection.getPatronId()).isEqualTo(snapshot.patronId());
        assertThat(projection.getSourceVersion()).isEqualTo(8);
        assertThat(projection.getAdministrativeBlock()).isTrue();

        assertThat(projection.getMembershipId()).isNull();
        assertThat(projection.getPlanVersionId()).isNull();
        assertThat(projection.getValidFrom()).isNull();
        assertThat(projection.getValidUntil()).isNull();
        assertThat(projection.getMaxCopies()).isNull();
        assertThat(projection.getMaxDays()).isNull();
    }

    @Test 
    public void newSnapshotReplacesOlder() {
        UUID patron = UUID.randomUUID();
        MembershipTerms membershipA = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-10-01T00:00:00Z"), 2, 14);
        PatronEligibilitySnapshot snapshotA = new PatronEligibilitySnapshot(patron, 5, true, membershipA);
        PatronEligibilityProjection projection = new PatronEligibilityProjection(snapshotA);

        MembershipTerms membershipB = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-02-01T00:00:00Z"), Instant.parse("2026-11-01T00:00:00Z"), 3, 21);
        PatronEligibilitySnapshot snapshotB = new PatronEligibilitySnapshot(patron, 8, false, membershipB);

        assertThat(projection.applyIfNewer(snapshotB)).isTrue();

        assertThat(projection.getSourceVersion()).isEqualTo(8);
        assertThat(projection.getAdministrativeBlock()).isFalse();

        assertThat(projection.getMembershipId()).isEqualTo(membershipB.membershipId());
        assertThat(projection.getPlanVersionId()).isEqualTo(membershipB.planVersionId());
        assertThat(projection.getValidFrom()).isEqualTo(membershipB.validFrom());
        assertThat(projection.getValidUntil()).isEqualTo(membershipB.validUntil());
        assertThat(projection.getMaxCopies()).isEqualTo(3);
        assertThat(projection.getMaxDays()).isEqualTo(21);
    }

    @ParameterizedTest 
    @ValueSource (longs = {7,8})
    public void olderOrEqualSnapshotIsIgnored(long incomingVersion) {
        UUID patron = UUID.randomUUID();
        MembershipTerms membershipA = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-10-01T00:00:00Z"), 2, 14);
        PatronEligibilitySnapshot snapshotA = new PatronEligibilitySnapshot(patron, 8, true, membershipA);
        PatronEligibilityProjection projection = new PatronEligibilityProjection(snapshotA);

        MembershipTerms membershipB = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-02-01T00:00:00Z"), Instant.parse("2026-11-01T00:00:00Z"), 3, 21);
        PatronEligibilitySnapshot snapshotB = new PatronEligibilitySnapshot(patron, incomingVersion, false, membershipB);

        assertThat(projection.applyIfNewer(snapshotB)).isFalse();

        assertThat(projection.getSourceVersion()).isEqualTo(8);
        assertThat(projection.getAdministrativeBlock()).isTrue();

        assertThat(projection.getMembershipId()).isEqualTo(membershipA.membershipId());
        assertThat(projection.getPlanVersionId()).isEqualTo(membershipA.planVersionId());
        assertThat(projection.getValidFrom()).isEqualTo(membershipA.validFrom());
        assertThat(projection.getValidUntil()).isEqualTo(membershipA.validUntil());
        assertThat(projection.getMaxCopies()).isEqualTo(2);
        assertThat(projection.getMaxDays()).isEqualTo(14);
    }

    @Test 
    public void differentPatronIsRejected() {
        MembershipTerms membershipA = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-10-01T00:00:00Z"), 2, 14);
        PatronEligibilitySnapshot snapshotA = new PatronEligibilitySnapshot(UUID.randomUUID(), 8, true, membershipA);
        PatronEligibilityProjection projection = new PatronEligibilityProjection(snapshotA);

        MembershipTerms membershipB = new MembershipTerms(UUID.randomUUID(), UUID.randomUUID(), Instant.parse("2026-02-01T00:00:00Z"), Instant.parse("2026-11-01T00:00:00Z"), 3, 15);
        PatronEligibilitySnapshot snapshotB = new PatronEligibilitySnapshot(UUID.randomUUID(), 9, false, membershipB);

        assertThatThrownBy(() -> projection.applyIfNewer(snapshotB))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(projection.getSourceVersion()).isEqualTo(8);
        assertThat(projection.getAdministrativeBlock()).isTrue();

        assertThat(projection.getMembershipId()).isEqualTo(membershipA.membershipId());
        assertThat(projection.getPlanVersionId()).isEqualTo(membershipA.planVersionId());
        assertThat(projection.getValidFrom()).isEqualTo(membershipA.validFrom());
        assertThat(projection.getValidUntil()).isEqualTo(membershipA.validUntil());
        assertThat(projection.getMaxCopies()).isEqualTo(2);
        assertThat(projection.getMaxDays()).isEqualTo(14);
    }
}