package com.project.library.patron.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class PatronService {

    private final PatronRepository patronRepository;

    public PatronService(PatronRepository aPatronRepository) {
        patronRepository = aPatronRepository;
    }

    @Transactional 
    @PreAuthorize ("hasRole('STAFF')")
    public PatronDetails createPatron(
        PatronType patronType, String firstName, String lastName, String phone
    ) {
        
        Patron patron = new Patron(patronType, firstName, lastName, phone);
        Patron savedPatron = patronRepository.saveAndFlush(patron);

        return new PatronDetails(
            savedPatron.getId(),
            savedPatron.getPatronType(),
            savedPatron.getFirstName(),
            savedPatron.getLastName(),
            savedPatron.getPhone()
        );
    }

    @Transactional (readOnly = true)
    @PreAuthorize ("hasRole('STAFF')")
    public Optional<PatronDetails> findPatron(UUID patronId) {
        return patronRepository.findById(patronId)
                .map(patron -> new PatronDetails(patron.getId(), patron.getPatronType(), patron.getFirstName(), patron.getLastName(), patron.getPhone()));
    }
}