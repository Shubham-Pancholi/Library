package com.project.library.circulation.internal;

import java.time.Instant;
import java.util.UUID;

import com.project.library.contracts.PatronEligibilitySnapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "patron_eligibility_projection", schema = "circulation")
public class PatronEligibilityProjection {

    @Id 
    private UUID patronId;

    @Column (name = "source_version", nullable = false)
    private long sourceVersion;

    @Column (name = "administrative_block", nullable = false)
    private boolean administrativeBlock;

    @Column (name = "membership_id")
    private UUID membershipId;

    @Column (name = "plan_version_id")
    private UUID planVersionId;

    @Column (name = "valid_from")
    private Instant validFrom;

    @Column (name = "valid_until")
    private Instant validUntil;

    @Column (name = "max_copies")
    private Integer maxCopies;

    @Column (name = "max_days")
    private Integer maxDays;

    protected PatronEligibilityProjection() {
    }

    public PatronEligibilityProjection(PatronEligibilitySnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Null snapshot passed for patron eligibility projection");
        }

        patronId = snapshot.patronId();
        sourceVersion = snapshot.sourceVersion();
        administrativeBlock = snapshot.administrativeBlock();

        if (snapshot.membership() != null) {
            membershipId = snapshot.membership().membershipId();
            planVersionId = snapshot.membership().planVersionId();

            validFrom = snapshot.membership().validFrom();
            validUntil = snapshot.membership().validUntil();

            maxCopies = snapshot.membership().maxCopies();
            maxDays = snapshot.membership().maxDays();
        }
    }

    public UUID getPatronId() {
        return patronId;
    }

    public long getSourceVersion() {
        return sourceVersion;
    }

    public boolean getAdministrativeBlock() {
        return administrativeBlock;
    }

    public UUID getMembershipId() {
        return membershipId;
    }

    public UUID getPlanVersionId() {
        return planVersionId;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Integer getMaxCopies() {
        return maxCopies;
    }

    public Integer getMaxDays() {
        return maxDays;
    }
}