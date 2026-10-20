package com.project.library.patron.internal;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.project.library.contracts.MembershipTerms;
import com.project.library.contracts.PatronEligibilitySnapshot;

@Component 
public class PatronEligibilitySnapshotFactory {

    private final MembershipRepository membershipRepository;
    private final MembershipPlanVersionRepository planVersionRepository;

    public PatronEligibilitySnapshotFactory(
        MembershipRepository aMembershipRepository,
        MembershipPlanVersionRepository aPlanVersionRepository
    ) {
        membershipRepository = aMembershipRepository;
        planVersionRepository = aPlanVersionRepository;
    }

    @Transactional (propagation = Propagation.MANDATORY)
    public PatronEligibilitySnapshot create(Patron patron, Instant at) {
        if (patron == null || patron.getId() == null || at == null) {
            throw new IllegalArgumentException("Invalid field(s) when creating a patron eligibility snapshot");
        }

        return new PatronEligibilitySnapshot(
            patron.getId(),
            patron.getEligibilityVersion(),
            patron.isAdministrativelyBlocked(),
            getMembership(patron.getId(), at));
    }

    private MembershipTerms getMembership(UUID patron, Instant at) {
        return MembershipSelector.selectCurrent(membershipRepository.findByPatronId(patron), at)
                .map(this :: toTerms)
                .orElse(null);
    }

    private MembershipTerms toTerms(Membership membership) {
        MembershipPlanVersion version = planVersionRepository.findById(membership.getPlanVersionId())
                                        .orElseThrow(() -> new IllegalStateException("Plan version with the id:" + membership.getPlanVersionId() + " is not found."));
        return new MembershipTerms(
            membership.getId(),
            membership.getPlanVersionId(),
            membership.getStartsAt(),
            membership.getEndsAt(),
            version.getMaxCopies(),
            version.getMaxDays()
        );
    }
}