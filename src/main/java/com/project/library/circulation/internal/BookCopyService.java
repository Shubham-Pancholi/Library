package com.project.library.circulation.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        BookCopy savedCopy = bookCopyRepository.save(copy);

        return toDetails(savedCopy);
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