package com.project.library.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MembershipTermsTests {

    @Test 
    public void validValuesAreRetained() {
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        MembershipTerms terms = new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays);

        assertThat(terms.membershipId()).isEqualTo(membership);
        assertThat(terms.planVersionId()).isEqualTo(version);
        assertThat(terms.validFrom()).isEqualTo(validFrom);
        assertThat(terms.validUntil()).isEqualTo(validUntil);
        assertThat(terms.maxCopies()).isEqualTo(maxCopies);
        assertThat(terms.maxDays()).isEqualTo(maxDays);
    }

    @ParameterizedTest 
    @ValueSource (strings = {"2026-01-01T00:00:00Z", "2025-12-31T00:00:00Z"})
    public void invalidDatesAreRejected(String validUntil) {
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        assertThatThrownBy(() -> new MembershipTerms(membership, version, validFrom, Instant.parse(validUntil), maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @ValueSource (ints = {0, -1})
    public void invalidCopyLimitsAreRejected(int maxCopies) {
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxDays = 21;

        assertThatThrownBy(() -> new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @ValueSource (ints = {0, -1})
    public void invalidDayLimitsAreRejected(int maxDays) {
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;

        assertThatThrownBy(() -> new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullMembershipIdThrows() {
        UUID membership = null;
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        assertThatThrownBy(() -> new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullVersionIdThrows() {
        UUID membership = UUID.randomUUID();
        UUID version = null;
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        assertThatThrownBy(() -> new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullValidFromThrows() {
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = null;
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        assertThatThrownBy(() -> new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullValidUntilThrows() {
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = null;
        int maxCopies = 3;
        int maxDays = 21;

        assertThatThrownBy(() -> new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays))
            .isInstanceOf(IllegalArgumentException.class);
    }
}