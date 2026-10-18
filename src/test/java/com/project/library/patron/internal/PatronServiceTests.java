package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import com.project.library.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;

@SpringBootTest 
@Import (
    {
        TestcontainersConfiguration.class,
        PatronServiceTests.Config.class
    }
)
@Transactional 
public class PatronServiceTests {

    @TestConfiguration 
    static class Config {
        @Bean 
        @Primary 
        public Clock getClock() {
            return Clock.fixed(Instant.parse("2026-10-10T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    private final PatronService patronService;
    private final PatronRepository patronRepository;
    private final AdministrativeBlockHistoryRepository administrativeBlockHistoryRepository;
    private final EntityManager entityManager;

    @Autowired 
    public PatronServiceTests(
        PatronService aPatronService,
        PatronRepository aPatronRepository,
        AdministrativeBlockHistoryRepository aAdministrativeBlockHistoryRepository,
        EntityManager aEntityManager
    ) {
        patronService = aPatronService;
        patronRepository = aPatronRepository;
        administrativeBlockHistoryRepository = aAdministrativeBlockHistoryRepository;
        entityManager = aEntityManager;
    }

    @Test 
    @WithMockUser (roles = "STAFF")
    public void staffCanCreateAndFindPatron() {

        PatronDetails patron = patronService.createPatron( PatronType.MEMBER, "Asha", "Rao", "0123456789");

        assertThat(patron.id()).isNotNull();
        assertThat(patron.patronType()).isEqualTo(PatronType.MEMBER);
        assertThat(patron.firstName()).isEqualTo("Asha");
        assertThat(patron.lastName()).isEqualTo("Rao");
        assertThat(patron.phone()).isEqualTo("0123456789");

        entityManager.flush();
        entityManager.clear();

        PatronDetails reloadedPatron = patronService.findPatron(patron.id())
                                    .orElseThrow();

        assertThat(reloadedPatron.id()).isEqualTo(patron.id());
        assertThat(reloadedPatron.patronType()).isEqualTo(patron.patronType());
        assertThat(reloadedPatron.firstName()).isEqualTo(patron.firstName());
        assertThat(reloadedPatron.lastName()).isEqualTo(patron.lastName());
        assertThat(reloadedPatron.phone()).isEqualTo(patron.phone());
    }

    @Test 
    @WithMockUser 
    public void nonStaffCannotCreatePatron() {
        long countBefore = patronRepository.count();

        assertThatThrownBy(() -> patronService.createPatron(PatronType.MEMBER, "Asha", "Rao", "0123456789"))
            .isInstanceOf(AccessDeniedException.class);

        long countAfter = patronRepository.count();

        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Test 
    public void unauthenticatedCallerCannotCreatePatron() {
        long countBefore = patronRepository.count();

        assertThatThrownBy(() -> patronService.createPatron(PatronType.MEMBER, "Asha", "Rao", "0123456789"))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);

        long countAfter = patronRepository.count();

        assertThat(countAfter).isEqualTo(countBefore);
    }

    @Test 
    @WithMockUser (roles = "STAFF")
    public void unknownPatronReturnsEmpty() {
        assertThat(patronService.findPatron(UUID.randomUUID())).isEmpty();
    }

    @Test 
    @WithMockUser 
    public void nonstaffCannotFindUser() {
        
        Patron patron = patronRepository.saveAndFlush(
            new Patron(PatronType.MEMBER, "Asha", "Rao", "0123456789")
        );

        entityManager.clear();

        assertThatThrownBy(() -> patronService.findPatron(patron.getId()))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test 
    public void staffCanBlockPatronAndRecordAudit() {
        createJwtStaffTokenAndAddItToContext();

        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);

        UUID id = patronRepository.saveAndFlush(patron).getId();

        entityManager.flush();
        entityManager.clear();

        patronService.blockPatron(id, "Loan overdue");

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(id).orElseThrow();

        assertThat(reloadedPatron.isAdministrativelyBlocked()).isTrue();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isEqualTo("Loan overdue");

        List<AdministrativeBlockHistory> records = administrativeBlockHistoryRepository.findAll();
        List<AdministrativeBlockHistory> patronRecords = new ArrayList<>();

        for (AdministrativeBlockHistory record : records) {
            if (record.getPatronId().equals(id)) {
                patronRecords.add(record);
            }
        }

        assertThat(patronRecords.size()).isEqualTo(1);

        AdministrativeBlockHistory record = patronRecords.get(0);

        assertThat(record.getId()).isNotNull();
        assertThat(record.getPatronId()).isEqualTo(id);
        assertThat(record.getAction()).isEqualTo(AdministrativeBlockAction.BLOCKED);
        assertThat(record.getReason()).isEqualTo("Loan overdue");
        assertThat(record.getChangedAt()).isEqualTo(Instant.parse("2026-10-10T00:00:00Z"));
        assertThat(record.getActorIssuer()).isEqualTo("http://com.project.library/realms/library");
        assertThat(record.getActorSubject()).isEqualTo("staff-test-subject");
    }

    @Test 
    public void blockingUnknownPatronThrows() {
        createJwtStaffTokenAndAddItToContext();

        UUID patron = UUID.randomUUID();

        long countBefore = administrativeBlockHistoryRepository.count();

        assertThatThrownBy(() -> patronService.blockPatron(patron, "loan overdue"))
            .isInstanceOf(PatronNotFoundException.class);
        
        long countAfter = administrativeBlockHistoryRepository.count();

        assertThat(countBefore).isEqualTo(countAfter);
    }

    @Test 
    public void blockingAlreadyBlockedPatronThrows() {
        createJwtStaffTokenAndAddItToContext();

        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);

        UUID id = patronRepository.saveAndFlush(patron).getId();

        patronService.blockPatron(id, "Reason A");

        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> patronService.blockPatron(id, "Reason B"))
            .isInstanceOf(AdministrativeBlockConflictException.class);

        entityManager.flush();
        entityManager.clear();

        Patron patronToCheck = patronRepository.findById(id).orElseThrow();

        assertThat(patronToCheck.isAdministrativelyBlocked()).isTrue();

        assertThat(patronToCheck.getAdministrativeBlockReason()).isEqualTo("Reason A");

        List<AdministrativeBlockHistory> records = administrativeBlockHistoryRepository.findAll();
        List<AdministrativeBlockHistory> patronRecords = new ArrayList<>();

        for (AdministrativeBlockHistory record : records) {
            if (record.getPatronId().equals(id)) {
                patronRecords.add(record);
            }
        }

        assertThat(patronRecords.size()).isEqualTo(1);
        assertThat(patronRecords.get(0).getReason()).isEqualTo("Reason A");
    }

    private void createJwtStaffTokenAndAddItToContext() {
        Jwt staffJwt = Jwt.withTokenValue("staff-test-token")
                        .header("alg", "RS256")
                        .issuer("http://com.project.library/realms/library")
                        .subject("staff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(staffJwt, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));

        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);
    }

    @Test 
    public void nonstaffCannotBlockPatron() {
        Jwt nonstaffJwt = Jwt.withTokenValue("nonstaff-test-token")
                        .header("alg", "RS256")
                        .issuer("http://com.project.library/realms/library")
                        .subject("nonstaff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of()))
                        .build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(nonstaffJwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);

        UUID id = patronRepository.saveAndFlush(patron).getId();

        long countBefore = administrativeBlockHistoryRepository.count();

        assertThatThrownBy(() -> patronService.blockPatron(id, "Test"))
            .isInstanceOf(AccessDeniedException.class);

        long countAfter = administrativeBlockHistoryRepository.count();

        assertThat(countBefore).isEqualTo(countAfter);

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(id).orElseThrow();

        assertThat(reloadedPatron.isAdministrativelyBlocked()).isFalse();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isNull();
    }

    @Test 
    public void staffWithoutValidIssuerCannotBlock() {
        Jwt nonstaffJwt = Jwt.withTokenValue("nonstaff-test-token")
                        .header("alg", "RS256")
                        .subject("nonstaff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of("STAFF")))
                        .build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(nonstaffJwt, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));

        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);

        UUID id = patronRepository.saveAndFlush(patron).getId();

        long countBefore = administrativeBlockHistoryRepository.count();

        assertThatThrownBy(() -> patronService.blockPatron(id, "Test"))
            .isInstanceOf(AccessDeniedException.class);

        long countAfter = administrativeBlockHistoryRepository.count();

        assertThat(countBefore).isEqualTo(countAfter);

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(id).orElseThrow();

        assertThat(reloadedPatron.isAdministrativelyBlocked()).isFalse();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isNull();
    }

    @Test 
    public void staffCanClearPatronBlock() {
        createJwtStaffTokenAndAddItToContext();

        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);

        UUID id = patronRepository.saveAndFlush(patron).getId();

        entityManager.flush();
        entityManager.clear();

        patronService.blockPatron(id, "Loan overdue");

        entityManager.flush();
        entityManager.clear();

        patronService.clearPatronBlock(id, "Loan paid");

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(id).orElseThrow();

        List<AdministrativeBlockHistory> records = administrativeBlockHistoryRepository.findAll();
        List<AdministrativeBlockHistory> patronRecords = new ArrayList<>();

        assertThat(reloadedPatron.isAdministrativelyBlocked()).isFalse();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isNull();

        for (AdministrativeBlockHistory record : records) {
            if (record.getPatronId().equals(id)) {
                patronRecords.add(record);
            }
        }

        assertThat(patronRecords.size()).isEqualTo(2);

        List<AdministrativeBlockHistory> record1 = patronRecords.stream().filter(r -> r.getAction().equals(AdministrativeBlockAction.BLOCKED)).toList();
        List<AdministrativeBlockHistory> record2 = patronRecords.stream().filter(r -> r.getAction().equals(AdministrativeBlockAction.CLEARED)).toList();

        assertThat(record1.size()).isEqualTo(1);
        assertThat(record2.size()).isEqualTo(1);

        assertThat(record1.get(0).getReason()).isEqualTo("Loan overdue");
        assertThat(record2.get(0).getReason()).isEqualTo("Loan paid");

        assertThat(record2.get(0).getChangedAt()).isEqualTo(Instant.parse("2026-10-10T00:00:00Z"));
        assertThat(record2.get(0).getActorIssuer()).isEqualTo("http://com.project.library/realms/library");
        assertThat(record2.get(0).getActorSubject()).isEqualTo("staff-test-subject");
    }

    @Test 
    public void clearingUnblockedPatronThrows() {
        createJwtStaffTokenAndAddItToContext();

        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);

        UUID id = patronRepository.saveAndFlush(patron).getId();

        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> patronService.clearPatronBlock(id, "Loan paid"))
            .isInstanceOf(AdministrativeBlockConflictException.class);

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(id).orElseThrow();

        assertThat(reloadedPatron.isAdministrativelyBlocked()).isFalse();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isNull();

        List<AdministrativeBlockHistory> records = administrativeBlockHistoryRepository.findAll();

        assertThat(records.size()).isEqualTo(0);
    }

    @Test 
    public void nonstaffCannotClearPatronBlock() {
        createJwtStaffTokenAndAddItToContext();

        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);

        UUID id = patronRepository.saveAndFlush(patron).getId();

        entityManager.flush();
        entityManager.clear();

        patronService.blockPatron(id, "Loan overdue");

        entityManager.flush();
        entityManager.clear();

        Jwt nonstaffJwt = Jwt.withTokenValue("nonstaff-test-token")
                        .header("alg", "RS256")
                        .issuer("http://com.project.library/realms/library")
                        .subject("nonstaff-test-subject")
                        .claim("scope", "openid")
                        .claim("realm_access", Map.of("roles", List.of()))
                        .build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(nonstaffJwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);

        assertThatThrownBy(() -> patronService.clearPatronBlock(id, "Loan paid"))
            .isInstanceOf(AccessDeniedException.class);

        entityManager.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(id).orElseThrow();

        assertThat(reloadedPatron.isAdministrativelyBlocked()).isTrue();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isEqualTo("Loan overdue");

        List<AdministrativeBlockHistory> records = administrativeBlockHistoryRepository.findAll();

        assertThat(records.size()).isEqualTo(1);

        assertThat(records.get(0).getAction()).isEqualTo(AdministrativeBlockAction.BLOCKED);
        assertThat(records.get(0).getReason()).isEqualTo("Loan overdue");
    }
}