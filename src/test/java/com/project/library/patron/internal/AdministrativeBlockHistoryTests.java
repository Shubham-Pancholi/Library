package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class AdministrativeBlockHistoryTests {

    @ParameterizedTest 
    @EnumSource (AdministrativeBlockAction.class)
    public void validInputsRetainsValues(AdministrativeBlockAction action) {
        UUID patron = UUID.randomUUID();
        String reason = "Reason";
        Instant changedAt = Instant.parse("2026-01-01T00:00:00Z");
        String actorIssuer = "Issuer";
        String actorSubject = "Subject";

        AdministrativeBlockHistory abh = new AdministrativeBlockHistory(patron, action, reason, changedAt, actorIssuer, actorSubject);

        assertThat(abh.getPatronId()).isEqualTo(patron);
        assertThat(abh.getReason()).isEqualTo(reason);
        assertThat(abh.getAction()).isEqualTo(action);
        assertThat(abh.getChangedAt()).isEqualTo(changedAt);
        assertThat(abh.getActorIssuer()).isEqualTo(actorIssuer);
        assertThat(abh.getActorSubject()).isEqualTo(actorSubject);
    }

    @Test 
    public void nullPatronIdIsRejected() {
        UUID patron = null;
        String reason = "Reason";
        AdministrativeBlockAction action = AdministrativeBlockAction.BLOCKED;
        Instant changedAt = Instant.parse("2026-01-01T00:00:00Z");
        String actorIssuer = "Issuer";
        String actorSubject = "Subject";

        assertThatThrownBy(() -> new AdministrativeBlockHistory(patron, action, reason, changedAt, actorIssuer, actorSubject))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullActionIsRejected() {
        UUID patron = UUID.randomUUID();
        String reason = "Reason";
        AdministrativeBlockAction action = null;
        Instant changedAt = Instant.parse("2026-01-01T00:00:00Z");
        String actorIssuer = "Issuer";
        String actorSubject = "Subject";

        assertThatThrownBy(() -> new AdministrativeBlockHistory(patron, action, reason, changedAt, actorIssuer, actorSubject))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void nullChangedTimeIsRejected() {
        UUID patron = UUID.randomUUID();
        String reason = "Reason";
        AdministrativeBlockAction action = AdministrativeBlockAction.BLOCKED;
        Instant changedAt = null;
        String actorIssuer = "Issuer";
        String actorSubject = "Subject";

        assertThatThrownBy(() -> new AdministrativeBlockHistory(patron, action, reason, changedAt, actorIssuer, actorSubject))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @NullAndEmptySource 
    @ValueSource (strings = {" ", "\t\n"})
    public void invalidReasonIsRejected(String reason) {
        UUID patron = UUID.randomUUID();
        AdministrativeBlockAction action = AdministrativeBlockAction.BLOCKED;
        Instant changedAt = Instant.parse("2026-01-01T00:00:00Z");
        String actorIssuer = "Issuer";
        String actorSubject = "Subject";

        assertThatThrownBy(() -> new AdministrativeBlockHistory(patron, action, reason, changedAt, actorIssuer, actorSubject))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @NullAndEmptySource 
    @ValueSource (strings = {" ", "\t\n"})
    public void invalidIssuerIsRejected(String issuer) {
        UUID patron = UUID.randomUUID();
        String reason = "Reason";
        AdministrativeBlockAction action = AdministrativeBlockAction.BLOCKED;
        Instant changedAt = Instant.parse("2026-01-01T00:00:00Z");
        String actorSubject = "Subject";

        assertThatThrownBy(() -> new AdministrativeBlockHistory(patron, action, reason, changedAt, issuer, actorSubject))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest 
    @NullAndEmptySource 
    @ValueSource (strings = {" ", "\t\n"})
    public void invalidSubjectIsRejected(String subject) {
        UUID patron = UUID.randomUUID();
        String reason = "Reason";
        AdministrativeBlockAction action = AdministrativeBlockAction.BLOCKED;
        Instant changedAt = Instant.parse("2026-01-01T00:00:00Z");
        String actorIssuer = "Issuer";

        assertThatThrownBy(() -> new AdministrativeBlockHistory(patron, action, reason, changedAt, actorIssuer, subject))
            .isInstanceOf(IllegalArgumentException.class);
    }
}