package com.project.library.contracts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PatronEligibilitySnapshotTests {

    @ParameterizedTest 
    @ValueSource (booleans = {false, true})
    public void nullMembershipIsAllowed(boolean block) {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 0;

        PatronEligibilitySnapshot peSnap = new PatronEligibilitySnapshot(patron, sourceVersion, block, null);

        assertThat(peSnap.patronId()).isEqualTo(patron);
        assertThat(peSnap.sourceVersion()).isEqualTo(sourceVersion);
        assertThat(peSnap.administrativeBlock()).isEqualTo(block);
        assertThat(peSnap.membership()).isNull();
    }

    @ParameterizedTest 
    @ValueSource (booleans = {false, true})
    public void validNonNullValuesArePassed(boolean block) {
        UUID patron = UUID.randomUUID();
        long sourceVersion = 1;
        
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        MembershipTerms term = new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays);

        PatronEligibilitySnapshot peSnap = new PatronEligibilitySnapshot(patron, sourceVersion, block, term);

        assertThat(peSnap.patronId()).isEqualTo(patron);
        assertThat(peSnap.sourceVersion()).isEqualTo(sourceVersion);
        assertThat(peSnap.administrativeBlock()).isEqualTo(block);
        assertThat(peSnap.membership()).isEqualTo(term);
    }

    @Test 
    public void nullPatronThrows() {
        UUID patron = null;
        long sourceVersion = 1;
        boolean block = true;
        
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        MembershipTerms term = new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays);

        assertThatThrownBy(() -> new PatronEligibilitySnapshot(patron, sourceVersion, block, term))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void negativeVersionThrows() {
        UUID patron = UUID.randomUUID();
        long sourceVersion = -1;
        boolean block = true;
        
        UUID membership = UUID.randomUUID();
        UUID version = UUID.randomUUID();
        Instant validFrom = Instant.parse("2026-01-01T00:00:00Z");
        Instant validUntil = Instant.parse("2026-10-19T14:00:00Z");
        int maxCopies = 3;
        int maxDays = 21;

        MembershipTerms term = new MembershipTerms(membership, version, validFrom, validUntil, maxCopies, maxDays);

        assertThatThrownBy(() -> new PatronEligibilitySnapshot(patron, sourceVersion, block, term))
            .isInstanceOf(IllegalArgumentException.class);
    }
}