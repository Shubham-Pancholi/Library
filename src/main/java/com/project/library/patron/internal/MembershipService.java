package com.project.library.patron.internal;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final Clock clock;

    public MembershipService(MembershipRepository aMembershipRepository, Clock aClock) {
        membershipRepository = aMembershipRepository;
        clock = aClock;
    }

    @Transactional (readOnly = true)
    @PreAuthorize ("hasRole('STAFF')")
    public Optional<MembershipDetails> findMembership(UUID membership) {
        return membershipRepository.findById(membership)
                .map(this :: toDetails);
    }

    private MembershipDetails toDetails(Membership membership) {
        return new MembershipDetails(
            membership.getId(),
            membership.getPatronId(),
            membership.getPlanVersionId(),
            membership.getStartsAt(),
            membership.getEndsAt(),
            membership.getMembershipStatus(),
            membership.isValidAt(clock.instant())
        );
    }
}