package com.project.library.catalog.internal;

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
public class BookRepositoryTests {

    @Autowired 
    private BookRepository bookRepository;

    @PersistenceContext 
    private EntityManager entityManager;

    @Test 
    public void savesAndReloadsBook() {
        
        Book book = new Book("The Hobbit");

        UUID generatedId = bookRepository.saveAndFlush(book).getId();

        assertThat(generatedId).isNotNull();
        entityManager.clear();
        Book reloadedBook = bookRepository.findById(generatedId).orElseThrow();

        assertThat(reloadedBook.getId()).isEqualTo(generatedId);
        assertThat(reloadedBook.getTitle()).isEqualTo("The Hobbit");
    }
}