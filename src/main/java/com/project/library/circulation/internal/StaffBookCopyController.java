package com.project.library.circulation.internal;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/staff/copies")
public class StaffBookCopyController {

    private final BookCopyService bookCopyService;

    public StaffBookCopyController(BookCopyService aBookCopyService) {
        bookCopyService = aBookCopyService;
    }

    @PostMapping 
    public ResponseEntity<BookCopyDetails> createBookCopy(
        @Valid @RequestBody CreateBookCopyRequest request
    ) {
        BookCopyDetails copy = bookCopyService.createCopy(request.catalogBookId(), request.accessionNumber(), request.barcode(), request.circulationType());
        return ResponseEntity.created(URI.create("/api/staff/copies/" + copy.id()))
                .body(copy);
    }

    @GetMapping ("/{id}")
    public ResponseEntity<BookCopyDetails> findBookCopy(
        @PathVariable UUID id
    ) {
        return ResponseEntity.of(bookCopyService.findCopy(id));
    }
}