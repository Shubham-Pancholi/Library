package com.project.library.catalog.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.library.catalog.BookDetails;
import com.project.library.catalog.CatalogApi;

@Service 
public class CatalogService implements CatalogApi{

    private final BookRepository bookRepository;

    public CatalogService (BookRepository aBookRepository) {
        bookRepository = aBookRepository;
    }

    @Override 
    @Transactional (readOnly = true)
    public Optional<BookDetails> findBook(UUID id) {
        return bookRepository.findById(id)
                .map(book -> new BookDetails(book.getId(), book.getTitle()));
    }
}