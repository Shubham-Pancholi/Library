package com.project.library.patron.internal;

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
@RequestMapping ("/api/staff/patrons")
public class StaffPatronController {

    private final PatronService patronService;

    public StaffPatronController(PatronService aPatronService) {
        patronService = aPatronService;
    }

    @PostMapping 
    public ResponseEntity<PatronDetails> createPatron(
        @Valid @RequestBody CreatePatronRequest request
    ) {
        PatronDetails patron = patronService.createPatron(request.patronType(), request.firstName(), request.lastName(), request.phone());
        return ResponseEntity.created(URI.create("/api/staff/patrons/" + patron.id())).body(patron);
    }

    @GetMapping ("/{id}")
    public ResponseEntity<PatronDetails> findPatron(
        @PathVariable UUID id
    ) {
        return ResponseEntity.of(patronService.findPatron(id));
    }

    @PostMapping ("/{id}/administrative-block")
    public ResponseEntity<Void> blockPatron(
        @PathVariable UUID id,
        @Valid @RequestBody AdministrativeBlockActionRequest request
    ) {
        patronService.blockPatron(id, request.reason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping ("/{id}/administrative-block/clear")
    public ResponseEntity<Void> clearPatronBlock(
        @PathVariable UUID id,
        @Valid @RequestBody AdministrativeBlockActionRequest request
    ) {
        patronService.clearPatronBlock(id, request.reason());
        return ResponseEntity.noContent().build();
    }
}