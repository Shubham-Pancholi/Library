package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import com.project.library.TestcontainersConfiguration;
import com.project.library.contracts.PatronEligibilityChanged;
import com.project.library.contracts.PatronEligibilitySnapshot;

import jakarta.persistence.EntityManager;

@SpringBootTest 
@Import (TestcontainersConfiguration.class)
@RecordApplicationEvents 
public class PatronBlockTransactionTests {

    @Autowired 
    private PatronService patronService;

    @Autowired 
    private PatronRepository patronRepository;

    @Autowired 
    private EntityManager entityManager;

    @Autowired 
    private ApplicationEvents events;

    @MockitoSpyBean 
    private AdministrativeBlockHistoryRepository historyRepository;

    @Test 
    public void auditFailureRollsBackPatronBlock() {

        Patron patron = new Patron(PatronType.MEMBER, "FName", "LName", "0");

        UUID patronId = patronRepository.saveAndFlush(patron).getId();

        try {
            Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                            .header("alg", "RS256")
                            .issuer("http://com.project.library/realms/library")
                            .subject("staff-test-subject")
                            .claim("scope", "openid")
                            .claim("realm_access", Map.of("roles", List.of("STAFF")))
                            .build();

            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(staffJwt, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));

            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

            doAnswer(invocation -> {
                entityManager.flush();
                throw new DataIntegrityViolationException("Simulated Audit Failure.");
            }).when(historyRepository).saveAndFlush(any());

            assertThatThrownBy(() -> patronService.blockPatron(patronId, "Testing"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Simulated Audit Failure.");

            Patron patron2 = patronRepository.findById(patronId).orElseThrow();

            assertThat(patron2.isAdministrativelyBlocked()).isFalse();
            assertThat(patron2.getAdministrativeBlockReason()).isNull();
            assertThat(patron2.getEligibilityVersion()).isEqualTo(0);

            List<AdministrativeBlockHistory> histories = historyRepository.findAll();
            List<AdministrativeBlockHistory> patronHistory = new ArrayList<>();

            for (AdministrativeBlockHistory history : histories) {
                if (history.getPatronId().equals(patronId)) {
                    patronHistory.add(history);
                }
            }

            assertThat(patronHistory.size()).isEqualTo(0);

            assertThat(events.stream(PatronEligibilityChanged.class).toList()).isEmpty();
        }
        finally {
            SecurityContextHolder.clearContext();
            patronRepository.deleteById(patronId);
        }
    }

    @Test 
    public void auditFailureRollsBackPatronClear() {
        Patron patron = new Patron(PatronType.MEMBER, "FName", "LName", "0");

        UUID patronId = patronRepository.saveAndFlush(patron).getId();
        UUID history = null;

        try {
            Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                            .header("alg", "RS256")
                            .issuer("http://com.project.library/realms/library")
                            .subject("staff-test-subject")
                            .claim("scope", "openid")
                            .claim("realm_access", Map.of("roles", List.of("STAFF")))
                            .build();

            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(staffJwt, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));

            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

            assertThat(patron.getEligibilityVersion()).isEqualTo(0);

            patronService.blockPatron(patronId, "Reason A");

            List<UUID> historyIds = historyRepository.findAll().stream().filter(r -> r.getPatronId().equals(patronId)).map(r-> r.getId()).toList();

            assertThat(historyIds.size()).isEqualTo(1);
            Patron afterBlock = patronRepository.findById(patronId).orElseThrow();
            assertThat(afterBlock.getEligibilityVersion()).isEqualTo(1);

            history = historyIds.get(0);

            doAnswer(invocation -> {
                entityManager.flush();
                throw new DataIntegrityViolationException("Simulated Audit Failure.");
            }).when(historyRepository).saveAndFlush(any());

            assertThatThrownBy(() -> patronService.clearPatronBlock(patronId, "Reason B"))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Simulated Audit Failure.");

            Patron patron2 = patronRepository.findById(patronId).orElseThrow();

            assertThat(patron2.isAdministrativelyBlocked()).isTrue();
            assertThat(patron2.getAdministrativeBlockReason()).isEqualTo("Reason A");
            assertThat(patron2.getEligibilityVersion()).isEqualTo(1);

            List<AdministrativeBlockHistory> histories = historyRepository.findAll();
            List<AdministrativeBlockHistory> patronHistory = new ArrayList<>();

            for (AdministrativeBlockHistory h : histories) {
                if (h.getPatronId().equals(patronId)) {
                    patronHistory.add(h);
                }
            }

            assertThat(patronHistory.size()).isEqualTo(1);

            AdministrativeBlockHistory record = patronHistory.get(0);

            assertThat(record.getId()).isEqualTo(history);
            assertThat(record.getAction()).isEqualTo(AdministrativeBlockAction.BLOCKED);
            assertThat(record.getReason()).isEqualTo("Reason A");

            List<PatronEligibilityChanged> publishedEvents = events.stream(PatronEligibilityChanged.class).toList();

            assertThat(publishedEvents.size()).isEqualTo(1);

            var publishedEvent = publishedEvents.get(0);

            assertThat(publishedEvent.snapshot()).isEqualTo(
                new PatronEligibilitySnapshot(patronId, 1, true, null)
            );
        }
        finally {
            SecurityContextHolder.clearContext();
            var fixtureHistory = historyRepository.findAll().stream()
                .filter(entry -> entry.getPatronId().equals(patronId))
                .toList();

            historyRepository.deleteAll(fixtureHistory);
            patronRepository.deleteById(patronId);
        }
    }
}