package com.project.library.patron.internal;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.project.library.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (TestcontainersConfiguration.class)
public class PatronRepositoryTests {

    @Autowired 
    private PatronRepository patronRepository;

    @PersistenceContext 
    private EntityManager entityManager;

    @Test 
    public void savesAndReloadsVisitor() {

        Patron patron = new Patron(PatronType.VISITOR, "Asha", null, null);

        UUID generatedId = patronRepository.saveAndFlush(patron).getId();

        assertThat(generatedId).isNotNull();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(generatedId).orElseThrow();

        assertThat(reloadedPatron.getId()).isEqualTo(generatedId);
        assertThat(reloadedPatron.getPatronType()).isEqualTo(PatronType.VISITOR);
        assertThat(reloadedPatron.getFirstName()).isEqualTo("Asha");
        assertThat(reloadedPatron.getLastName()).isNull();
        assertThat(reloadedPatron.getPhone()).isNull();
        assertThat(reloadedPatron.isAdministrativelyBlocked()).isFalse();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isNull();
    }

    @Test 
    public void savesAndReloadsMember() {
        
        Patron patron = new Patron(PatronType.MEMBER, "Asha", "Rao", "0987654321");

        UUID generatedId = patronRepository.saveAndFlush(patron).getId();

        assertThat(generatedId).isNotNull();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(generatedId).orElseThrow();

        assertThat(reloadedPatron.getId()).isEqualTo(generatedId);
        assertThat(reloadedPatron.getPatronType()).isEqualTo(PatronType.MEMBER);
        assertThat(reloadedPatron.getFirstName()).isEqualTo("Asha");
        assertThat(reloadedPatron.getLastName()).isEqualTo("Rao");
        assertThat(reloadedPatron.getPhone()).isEqualTo("0987654321");
        assertThat(reloadedPatron.isAdministrativelyBlocked()).isFalse();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isNull();
    }

    @Test 
    public void blockMechanismTests() {
        Patron patron = new Patron(PatronType.MEMBER, "FName", null, null);

        UUID patronId = patronRepository.save(patron).getId();

        patron.blockAdministratively("Testing");

        patronRepository.flush();
        entityManager.clear();

        Patron loadedPatron = patronRepository.findById(patronId).orElseThrow();

        assertThat(loadedPatron.isAdministrativelyBlocked()).isTrue();
        assertThat(loadedPatron.getAdministrativeBlockReason()).isNotNull().isEqualTo("Testing");

        loadedPatron.clearAdministrativeBlock();

        patronRepository.flush();
        entityManager.clear();

        Patron reloadedPatron = patronRepository.findById(patronId).orElseThrow();

        assertThat(reloadedPatron.isAdministrativelyBlocked()).isFalse();
        assertThat(reloadedPatron.getAdministrativeBlockReason()).isNull();
    }
}