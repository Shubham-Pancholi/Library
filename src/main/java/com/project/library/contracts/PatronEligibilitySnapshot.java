package com.project.library.contracts;

import java.util.UUID;

public record PatronEligibilitySnapshot(
    UUID patronId,
    long sourceVersion,
    boolean administrativeBlock,
    MembershipTerms membership
) {
    public PatronEligibilitySnapshot {
        if (patronId == null || sourceVersion < 0) {
            throw new IllegalArgumentException("Invalid field(s) for Patron eligibility snapshot");
        }
    }
}