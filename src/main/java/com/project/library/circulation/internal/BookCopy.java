package com.project.library.circulation.internal;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "book_copy", schema = "circulation")
public class BookCopy {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (name = "catalog_book_id", nullable = false)
    private UUID catalogBookId;

    @Column (columnDefinition = "text", nullable = false, updatable = false)
    private String accessionNumber;

    @Column (columnDefinition = "text")
    private String barcode;

    @Enumerated (EnumType.STRING)
    @Column (name = "circulation_type", nullable = false, length = 16)
    private CirculationType circulationType;

    @Enumerated (EnumType.STRING)
    @Column (name = "availability_status", nullable = false, length = 16)
    private CopyAvailabilityStatus copyAvailabilityStatus;

    protected BookCopy() {
        //
    }

    public BookCopy (
        UUID aCatalogBookId,
        String aAccessionNumber,
        String aBarcode,
        CirculationType aCirculationType
    ) {
        if (aCatalogBookId == null || aAccessionNumber == null || aAccessionNumber.isBlank() || (aBarcode == null ? false : aBarcode.isBlank()) || aCirculationType == null ) {
            throw new IllegalArgumentException("Invalid Book copy field(s)");
        }

        catalogBookId = aCatalogBookId;
        accessionNumber = aAccessionNumber;
        barcode = aBarcode;
        circulationType = aCirculationType;
        copyAvailabilityStatus = CopyAvailabilityStatus.AVAILABLE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCatalogBookId() {
        return catalogBookId;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public String getBarcode() {
        return barcode;
    }

    public CirculationType getCirculationType() {
        return circulationType;
    }

    public CopyAvailabilityStatus getCopyAvailabilityStatus() {
        return copyAvailabilityStatus;
    }
}