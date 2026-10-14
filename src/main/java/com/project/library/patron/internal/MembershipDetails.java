package com.project.library.patron.internal;

import java.time.Instant;
import java.util.UUID;

public record MembershipDetails(
    UUID id,
    UUID patronId,
    UUID planVersionId,
    Instant startsAt,
    Instant endsAt,
    MembershipStatus status,
    boolean currentlyValid
) {}