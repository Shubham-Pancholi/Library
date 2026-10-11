package com.project.library.catalog.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.project.library.TestcontainersConfiguration;
import com.project.library.catalog.BookDetails;
import com.project.library.catalog.CatalogApi;

import jakarta.persistence.EntityManager;

@DataJpaTest 
@AutoConfigureTestDatabase (replace = AutoConfigureTestDatabase.Replace.NONE)
@Import ({TestcontainersConfiguration.class, CatalogService.class})
public class CatalogServiceTests {

    private final BookRepository bookRepository;
    private final CatalogApi catalogApi;
    private final EntityManager entityManager;
    private final CatalogService catalogService;

    @Autowired 
    public CatalogServiceTests(
        BookRepository aBookRepository,
        CatalogApi aCatalogApi,
        EntityManager aEntityManager,
        CatalogService aCatalogService
    ) {
        bookRepository = aBookRepository;
        catalogApi = aCatalogApi;
        entityManager = aEntityManager;
        catalogService = aCatalogService;
    }

    @Test 
    public void returnsDetailsForExistingBook() {
        Book book = new Book("The Hobbit");
        UUID generatedId = bookRepository.saveAndFlush(book).getId();

        entityManager.clear();

        Optional<BookDetails> result = catalogApi.findBook(generatedId);

        assertThat(result).contains(new BookDetails(generatedId, "The Hobbit"));
    }

    @Test 
    public void returnsEmptyForUnknownBook() {
        Optional<BookDetails> result = catalogApi.findBook(UUID.randomUUID());
        assertThat(result).isEmpty();
    }

    @Test 
    public void createsAndPersistsBook() {
        String title = "The Hobbit";
        BookDetails createdBook = catalogService.createBook(title);

        assertThat(createdBook.id()).isNotNull();
        assertThat(createdBook.title()).isEqualTo(title);

        entityManager.flush();
        entityManager.clear();

        Book reloadedBook = bookRepository.findById(createdBook.id()).orElseThrow();

        assertThat(reloadedBook.getId()).isEqualTo(createdBook.id());
        assertThat(reloadedBook.getTitle()).isEqualTo(title);
    }
}