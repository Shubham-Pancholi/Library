package com.project.library.catalog.internal;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.library.catalog.BookDetails;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/staff/books")
public class StaffBookController {

    private final CatalogService catalogService;

    public StaffBookController(CatalogService aCatalogService) {
        catalogService = aCatalogService;
    }

    @PostMapping 
    public ResponseEntity<BookDetails> createBook(
        @Valid @RequestBody CreateBookRequest request
    ) {
        BookDetails book = catalogService.createBook(request.title());
        return ResponseEntity.created(URI.create("/api/books/" + book.id())).body(book);
    }
}