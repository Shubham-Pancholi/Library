package com.project.library.patron.internal;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class PatronService {

    private final PatronRepository patronRepository;
    private final AdministrativeBlockHistoryRepository administrativeBlockHistoryRepository;
    private Clock clock;

    public PatronService(PatronRepository aPatronRepository, AdministrativeBlockHistoryRepository aAdministrativeBlockHistoryRepository, Clock aClock) {
        patronRepository = aPatronRepository;
        administrativeBlockHistoryRepository = aAdministrativeBlockHistoryRepository;
        clock = aClock;
    }

    @Transactional 
    @PreAuthorize ("hasRole('STAFF')")
    public PatronDetails createPatron(
        PatronType patronType, String firstName, String lastName, String phone
    ) {
        
        Patron patron = new Patron(patronType, firstName, lastName, phone);
        Patron savedPatron = patronRepository.saveAndFlush(patron);

        return new PatronDetails(
            savedPatron.getId(),
            savedPatron.getPatronType(),
            savedPatron.getFirstName(),
            savedPatron.getLastName(),
            savedPatron.getPhone()
        );
    }

    @Transactional (readOnly = true)
    @PreAuthorize ("hasRole('STAFF')")
    public Optional<PatronDetails> findPatron(UUID patronId) {
        return patronRepository.findById(patronId)
                .map(patron -> new PatronDetails(patron.getId(), patron.getPatronType(), patron.getFirstName(), patron.getLastName(), patron.getPhone()));
    }

    @Transactional 
    @PreAuthorize ("hasRole('STAFF')")
    public void blockPatron(UUID patronId, String reason) {
        if (patronId == null || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Invalid vaules Passed for blocking a patron");
        }

        JwtAuthenticationToken auth = getJwtToken();

        String issuer = auth.getToken().getIssuer().toString();
        String subject = auth.getToken().getSubject();

        Patron patron = patronRepository.findByIdForUpdate(patronId)
                        .orElseThrow(() -> new PatronNotFoundException("Patron with id " + patronId + " was not in the records"));

        patron.blockAdministratively(reason);

        AdministrativeBlockHistory history = new AdministrativeBlockHistory(
            patronId,
            AdministrativeBlockAction.BLOCKED,
            reason,
            clock.instant(),
            issuer,
            subject
        );

        administrativeBlockHistoryRepository.saveAndFlush(history);
    }

    @Transactional 
    @PreAuthorize ("hasRole('STAFF')")
    public void clearPatronBlock(UUID patronId, String reason) {
        if (patronId == null || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Invalid vaules Passed for clearing a patron block");
        }

        JwtAuthenticationToken auth = getJwtToken();

        String issuer = auth.getToken().getIssuer().toString();
        String subject = auth.getToken().getSubject();

        Patron patron = patronRepository.findByIdForUpdate(patronId)
                        .orElseThrow(() -> new PatronNotFoundException("Patron with id " + patronId + " was not in the records"));

        patron.clearAdministrativeBlock();

        AdministrativeBlockHistory history = new AdministrativeBlockHistory(
            patronId,
            AdministrativeBlockAction.CLEARED,
            reason,
            clock.instant(),
            issuer,
            subject
        );

        administrativeBlockHistoryRepository.saveAndFlush(history);
    }

    private JwtAuthenticationToken getJwtToken() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
            if (jwtAuth.isAuthenticated()) {
                Jwt jwt = jwtAuth.getToken();
                if (jwt.getIssuer() != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
                    return jwtAuth;
                }
            }
        }
        throw new AccessDeniedException("User does not have access to block a patron");
    }
}