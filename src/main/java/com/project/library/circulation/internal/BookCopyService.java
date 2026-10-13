package com.project.library.circulation.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import com.project.library.catalog.CatalogApi;

@Service 
public class BookCopyService {

    private final BookCopyRepository bookCopyRepository;
    private final CatalogApi catalogApi;

    public BookCopyService(BookCopyRepository aBookCopyRepository, CatalogApi aCatalogApi) {
        bookCopyRepository = aBookCopyRepository;
        catalogApi = aCatalogApi;
    }

    @Transactional 
    @PreAuthorize ("hasRole('STAFF')")
    public BookCopyDetails createCopy(UUID catalogBookId, String accessionNumber, String barcode, CirculationType circulationType) {
        
        BookCopy copy = new BookCopy(catalogBookId, accessionNumber, barcode, circulationType);

        catalogApi.findBook(catalogBookId).orElseThrow(() -> new CatalogBookNotFoundException(catalogBookId));

        try {
            BookCopy savedCopy = bookCopyRepository.saveAndFlush(copy);

            return toDetails(savedCopy);
        }   catch(DataIntegrityViolationException exception) {
            duplicateIdentifier(exception)
            .ifPresent(i -> { throw new DuplicateCopyIdentifierException(i, exception); });

            throw exception;
        }
    }

    private Optional<String> duplicateIdentifier(Throwable exception) {
        for (Throwable cause = exception; cause != null ; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException cve) {
                if ("23505".equals(cve.getSQLState())) {
                    if ("book_copy_accession_number_key".equals(cve.getConstraintName())) return Optional.of("Accession Number");
                    if ("book_copy_barcode_key".equals(cve.getConstraintName())) return Optional.of("Barcode");
                }
            }
        }
        return Optional.empty();
    }

    @Transactional (readOnly = true)
    @PreAuthorize ("hasRole('STAFF')")
    public Optional<BookCopyDetails> findCopy(UUID copyId) {
        
        return bookCopyRepository.findById(copyId)
            .map(this :: toDetails);
    }

    private BookCopyDetails toDetails(BookCopy copy) {
        return new BookCopyDetails(
            copy.getId(),
            copy.getCatalogBookId(),
            copy.getAccessionNumber(),
            copy.getBarcode(),
            copy.getCirculationType(),
            copy.getCopyAvailabilityStatus()
        );
    }
}