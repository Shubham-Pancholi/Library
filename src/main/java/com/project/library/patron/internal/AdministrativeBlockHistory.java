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
@Table (name = "administrative_block_history", schema = "patron")
public class AdministrativeBlockHistory {

    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (name = "patron_id", nullable = false)
    private UUID patronId;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false, length = 20)
    private AdministrativeBlockAction action;

    @Column (nullable = false, columnDefinition = "text")
    private String reason;

    @Column (name = "changed_at", nullable = false)
    private Instant changedAt;

    @Column (name = "actor_issuer", nullable = false, columnDefinition = "text")
    private String actorIssuer;

    @Column (name = "actor_subject", nullable = false, columnDefinition = "text")
    private String actorSubject;

    protected AdministrativeBlockHistory() {
    }

    public AdministrativeBlockHistory(
        UUID aPatronId, AdministrativeBlockAction aAction, String aReason, Instant aChangedAt, String aActorIssuer, String aActorSubject
    ) {
        if (aPatronId == null || aAction == null || aReason == null || aReason.isBlank() || aChangedAt == null || aActorIssuer == null || aActorIssuer.isBlank() || aActorSubject == null || aActorSubject.isBlank()) {
            throw new IllegalArgumentException("Invalid Administrative blocked history field(s)");
        }

        patronId = aPatronId;
        action = aAction;
        reason = aReason;
        changedAt = aChangedAt;
        actorIssuer = aActorIssuer;
        actorSubject = aActorSubject;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPatronId() {
        return patronId;
    }

    public AdministrativeBlockAction getAction() {
        return action;
    }

    public String getReason() {
        return reason;
    }

    public Instant getChangedAt() {
        return changedAt;
    }

    public String getActorIssuer() {
        return actorIssuer;
    }

    public String getActorSubject() {
        return actorSubject;
    }
}