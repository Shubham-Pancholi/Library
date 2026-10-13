package com.project.library.circulation.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.project.library.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (TestcontainersConfiguration.class)
public class BookCopyRepositoryTests {

    @Autowired 
    private BookCopyRepository bookCopyRepository;

    @PersistenceContext 
    private EntityManager entityManager;

    @Test 
    public void savesAndReloadsCopy(){
        UUID catalogBookId = UUID.randomUUID();

        BookCopy copy = new BookCopy(catalogBookId, "ACC-001", "BC-001", CirculationType.REFERENCE_ONLY);

        BookCopy savedCopy = bookCopyRepository.saveAndFlush(copy);

        assertThat(savedCopy.getId()).isNotNull();

        entityManager.clear();

        assertThat(savedCopy.getCatalogBookId()).isEqualTo(catalogBookId);
        assertThat(savedCopy.getAccessionNumber()).isEqualTo("ACC-001");
        assertThat(savedCopy.getBarcode()).isEqualTo("BC-001");
        assertThat(savedCopy.getCirculationType()).isEqualTo(CirculationType.REFERENCE_ONLY);
        assertThat(savedCopy.getCopyAvailabilityStatus()).isEqualTo(CopyAvailabilityStatus.AVAILABLE);

        BookCopy reloadedCopy = bookCopyRepository.findById(savedCopy.getId())
                                .orElseThrow();

        assertThat(reloadedCopy.getId()).isEqualTo(savedCopy.getId());
        assertThat(reloadedCopy.getCatalogBookId()).isEqualTo(catalogBookId);
        assertThat(reloadedCopy.getAccessionNumber()).isEqualTo("ACC-001");
        assertThat(reloadedCopy.getBarcode()).isEqualTo("BC-001");
        assertThat(reloadedCopy.getCirculationType()).isEqualTo(CirculationType.REFERENCE_ONLY);
        assertThat(reloadedCopy.getCopyAvailabilityStatus()).isEqualTo(CopyAvailabilityStatus.AVAILABLE);
    }

    @Test 
    public void rejectsDuplicateAccessionNumber() {
        UUID catalogBookId = UUID.randomUUID();

        BookCopy copy = new BookCopy(catalogBookId, "ACC-001", "BC-001", CirculationType.REFERENCE_ONLY);
        bookCopyRepository.saveAndFlush(copy);

        BookCopy duplicate = new BookCopy(catalogBookId, "ACC-001", "BC-002", CirculationType.CIRCULATING);

        assertThatThrownBy(() -> bookCopyRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test 
    public void rejectsDuplicatebarcode() {
        UUID catalogBookId = UUID.randomUUID();

        BookCopy copy = new BookCopy(catalogBookId, "ACC-001", "BC-001", CirculationType.REFERENCE_ONLY);
        bookCopyRepository.saveAndFlush(copy);

        BookCopy duplicate = new BookCopy(catalogBookId, "ACC-002", "BC-001", CirculationType.CIRCULATING);

        assertThatThrownBy(() -> bookCopyRepository.saveAndFlush(duplicate))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test 
    public void allowsMultipleCopiesWithoutBarcode() {
        UUID catalogBookId = UUID.randomUUID();

        BookCopy copy1 = new BookCopy(catalogBookId, "ACC-001", null, CirculationType.STAFF_ONLY);
        BookCopy savedCopy1 = bookCopyRepository.saveAndFlush(copy1);

        assertThat(savedCopy1.getId()).isNotNull();

        BookCopy copy2 = new BookCopy(catalogBookId, "ACC-002", null, CirculationType.ARCHIVE);
        BookCopy savedCopy2 = bookCopyRepository.saveAndFlush(copy2);

        assertThat(savedCopy2.getId()).isNotNull();

        entityManager.clear();

        assertThat(savedCopy1.getId()).isNotEqualTo(savedCopy2.getId());

        BookCopy reloadedCopy1 = bookCopyRepository.findById(savedCopy1.getId())
                                    .orElseThrow();

        BookCopy reloadedCopy2 = bookCopyRepository.findById(savedCopy2.getId())
                                    .orElseThrow();

        assertThat(reloadedCopy1.getCatalogBookId()).isEqualTo(reloadedCopy2.getCatalogBookId()).isEqualTo(catalogBookId);
        assertThat(reloadedCopy1.getAccessionNumber()).isEqualTo("ACC-001");
        assertThat(reloadedCopy2.getAccessionNumber()).isEqualTo("ACC-002");
        assertNull(reloadedCopy1.getBarcode());
        assertNull(reloadedCopy2.getBarcode());
    }
}