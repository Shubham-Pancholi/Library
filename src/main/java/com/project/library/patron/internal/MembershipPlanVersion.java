package com.project.library.patron.internal;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "membership_plan_version", schema = "patron")
public class MembershipPlanVersion {

    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (name = "plan_id", nullable = false)
    private UUID planId;

    @Column (name = "effective_from", nullable = false)
    private Instant effectiveFrom;

    @Column (name = "effective_to")
    private Instant effectiveTo;

    @Column (name = "max_copies", nullable = false)
    private int maxCopies;

    @Column (name = "max_days", nullable = false)
    private int maxDays;

    protected MembershipPlanVersion() {
    }

    public MembershipPlanVersion(UUID aPlanId, Instant aEffectiveFrom, Instant aEffectiveTo, int aMaxCopies, int aMaxDays) {
        if (aPlanId == null || aEffectiveFrom == null || aMaxCopies <= 0 || aMaxDays <= 0 || (aEffectiveTo == null ? false : !aEffectiveTo.isAfter(aEffectiveFrom))) {
            throw new IllegalArgumentException("Invalid Membership Plan Version field(s)");
        }
        planId = aPlanId;
        effectiveFrom = aEffectiveFrom;
        effectiveTo = aEffectiveTo;
        maxCopies = aMaxCopies;
        maxDays = aMaxDays;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPlanId() {
        return planId;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public Instant getEffectiveTo() {
        return effectiveTo;
    }

    public int getMaxCopies() {
        return maxCopies;
    }

    public int getMaxDays() {
        return maxDays;
    }
}