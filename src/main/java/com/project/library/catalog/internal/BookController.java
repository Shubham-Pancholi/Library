package com.project.library.catalog.internal;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.library.catalog.BookDetails;
import com.project.library.catalog.CatalogApi;

@RestController 
@RequestMapping ("/api/books")
public class BookController {

    private final CatalogApi catalogApi;

    public BookController(CatalogApi aCatalogApi) {
        catalogApi = aCatalogApi;
    }

    @GetMapping ("/{bookId}")
    public ResponseEntity<BookDetails> findBook(
        @PathVariable UUID bookId
    ) {
        return ResponseEntity.of(catalogApi.findBook(bookId));
    }
}