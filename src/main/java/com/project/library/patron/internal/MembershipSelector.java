package com.project.library.patron.internal;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class MembershipSelector {

    static Optional<Membership> selectCurrent(
        List<Membership> memberships,
        Instant at
    ) {
        if (memberships == null || at == null) {
            throw new IllegalArgumentException("Invalid Field(s) while getting membership");
        }

        List<Membership> validMemberships = memberships.stream().filter(m -> m.isValidAt(at)).toList();

        if (validMemberships.size() > 1) {
            throw new IllegalStateException("More than one valid membership at the instant. Invalid State.");
        }

        return validMemberships.stream().findFirst();
    }
}