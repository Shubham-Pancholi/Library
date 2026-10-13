package com.project.library.circulation.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.project.library.TestcontainersConfiguration;
import com.project.library.catalog.BookDetails;
import com.project.library.catalog.CatalogApi;

import jakarta.persistence.EntityManager;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import (
    {
        TestcontainersConfiguration.class,
        BookCopyService.class,
        BookCopyServiceTests.MethodSecurityConfiguration.class
    }
)
public class BookCopyServiceTests {

    @TestConfiguration (proxyBeanMethods = false)
    @EnableMethodSecurity 
    static class MethodSecurityConfiguration {
    }

    private final BookCopyService bookCopyService;
    private final BookCopyRepository bookCopyRepository;
    private final EntityManager entityManager;

    @MockitoBean 
    private CatalogApi catalogApi;

    @Autowired 
    public BookCopyServiceTests(
        BookCopyService aBookCopyService,
        BookCopyRepository aBookCopyRepository,
        EntityManager aEntityManager
    ) {
        bookCopyService = aBookCopyService;
        bookCopyRepository = aBookCopyRepository;
        entityManager = aEntityManager;
    }

    @Test 
    @WithMockUser (roles = "STAFF")
    public void staffCanCreateAndFindCopy() {
        UUID catalogBookId = UUID.randomUUID();
        when(catalogApi.findBook(catalogBookId)).thenReturn(Optional.of(new BookDetails(catalogBookId, "The Hobbit")));

        BookCopyDetails bookCopy = bookCopyService.createCopy(catalogBookId, "ACC-001", "BC-001", CirculationType.CIRCULATING);

        assertThat(bookCopy.id()).isNotNull();

        entityManager.flush();
        entityManager.clear();

        assertThat(bookCopy.catalogBookId()).isEqualTo(catalogBookId);
        assertThat(bookCopy.accessionNumber()).isEqualTo("ACC-001");
        assertThat(bookCopy.barcode()).isEqualTo("BC-001");
        assertThat(bookCopy.circulationType()).isEqualTo(CirculationType.CIRCULATING);
        assertThat(bookCopy.availabilityStatus()).isEqualTo(CopyAvailabilityStatus.AVAILABLE);

        BookCopyDetails loadedCopy = bookCopyService.findCopy(bookCopy.id())
            .orElseThrow();
        
        assertThat(loadedCopy.id()).isEqualTo(bookCopy.id());
        assertThat(loadedCopy.catalogBookId()).isEqualTo(catalogBookId);
        assertThat(loadedCopy.accessionNumber()).isEqualTo("ACC-001");
        assertThat(loadedCopy.barcode()).isEqualTo("BC-001");
        assertThat(loadedCopy.circulationType()).isEqualTo(CirculationType.CIRCULATING);
        assertThat(loadedCopy.availabilityStatus()).isEqualTo(CopyAvailabilityStatus.AVAILABLE);

        verify(catalogApi).findBook(catalogBookId);
    }

    @Test 
    @WithMockUser (roles = "STAFF")
    public void unknownCatalogBookRejectsCreation() {
        UUID catalogBookId = UUID.randomUUID();
        when(catalogApi.findBook(catalogBookId)).thenReturn(Optional.empty());

        long countBefore = bookCopyRepository.count();

        assertThatThrownBy(() -> bookCopyService.createCopy(catalogBookId, "AC-001", null, CirculationType.ARCHIVE))
            .isInstanceOf(CatalogBookNotFoundException.class);

        long countAfter = bookCopyRepository.count();

        assertThat(countBefore).isEqualTo(countAfter);
    }

    @Test 
    @WithMockUser (roles = "STAFF")
    public void unknownCopyReturnsEmpty() {
        UUID copyId = UUID.randomUUID();

        assertThat(bookCopyService.findCopy(copyId)).isEmpty();

        verifyNoInteractions(catalogApi);
    }

    @Test 
    @WithMockUser 
    public void nonStaffCannotCreateOrFindCopies() {
        long countBefore = bookCopyRepository.count();

        assertThatThrownBy(() -> bookCopyService.createCopy(UUID.randomUUID(), "AC-001", null, CirculationType.ARCHIVE))
            .isInstanceOf(AccessDeniedException.class);

        entityManager.flush();
        entityManager.clear();

        long countAfter = bookCopyRepository.count();

        assertThat(countBefore).isEqualTo(countAfter);

        assertThatThrownBy(() -> bookCopyService.findCopy(UUID.randomUUID()))
            .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(catalogApi);
    }
}