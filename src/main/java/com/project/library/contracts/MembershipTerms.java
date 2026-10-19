package com.project.library.contracts;

import java.time.Instant;
import java.util.UUID;

public record MembershipTerms(
    UUID membershipId,
    UUID planVersionId,
    Instant validFrom,
    Instant validUntil,
    int maxCopies,
    int maxDays
) {
    public MembershipTerms {
        if (membershipId == null || planVersionId == null || validFrom == null || validUntil == null || !validUntil.isAfter(validFrom) || maxCopies <= 0 || maxDays <= 0) {
            throw new IllegalArgumentException("Invalid field(s) for Membership terms");
        }
    }
}