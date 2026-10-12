package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;

import com.project.library.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;

@SpringBootTest 
@Import (TestcontainersConfiguration.class)
@Transactional 
public class PatronServiceTests {

    private final PatronService patronService;
    private final PatronRepository patronRepository;
    private final EntityManager entityManager;

    @Autowired 
    public PatronServiceTests(
        PatronService aPatronService,
        PatronRepository aPatronRepository,
        EntityManager aEntityManager
    ) {
        patronService = aPatronService;
        patronRepository = aPatronRepository;
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
}