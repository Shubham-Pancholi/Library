package com.project.library.patron.internal;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "membership", schema = "patron")
public class Membership {

    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (name = "patron_id", nullable = false)
    private UUID patronId;

    @Column (name = "plan_version_id", nullable = false)
    private UUID planVersionId;

    @Column (name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column (name = "ends_at", nullable = false)
    private Instant endsAt;

    @Enumerated (EnumType.STRING)
    @Column (name = "status", nullable = false, length = 16)
    private MembershipStatus membershipStatus;

    protected Membership() {
    }

    public Membership(UUID aPatronId, UUID aPlanVersionId, Instant aStartsAt, Instant aEndsAt, MembershipStatus aMembershipStatus) {
        if (aPatronId == null || aPlanVersionId == null || aStartsAt == null || aEndsAt == null || !aEndsAt.isAfter(aStartsAt) || aMembershipStatus == null) {
            throw new IllegalArgumentException("Invalid Membership field(s)");
        }

        patronId = aPatronId;
        planVersionId = aPlanVersionId;
        startsAt = aStartsAt;
        endsAt = aEndsAt;
        membershipStatus = aMembershipStatus;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPatronId() {
        return patronId;
    }

    public UUID getPlanVersionId() {
        return planVersionId;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public MembershipStatus getMembershipStatus() {
        return membershipStatus;
    }
}