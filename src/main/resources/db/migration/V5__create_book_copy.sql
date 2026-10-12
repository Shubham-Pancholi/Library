CREATE TABLE circulation.book_copy (
    id UUID NOT NULL PRIMARY KEY,
    catalog_book_id UUID NOT NULL,
    accession_number TEXT NOT NULL UNIQUE CONSTRAINT accession_number_not_blank CHECK (btrim(accession_number) <> ''),
    barcode TEXT UNIQUE CONSTRAINT barcode_not_blank CHECK (barcode IS NULL OR btrim(barcode) <> ''),
    circulation_type VARCHAR(16) NOT NULL CONSTRAINT circulation_type_enum CHECK (circulation_type IN ('CIRCULATING','REFERENCE_ONLY', 'ARCHIVE', 'STAFF_ONLY')),
    availability_status VARCHAR(16) NOT NULL CONSTRAINT availability_status_enum CHECK (availability_status IN ('AVAILABLE','ON_LOAN','ON_HOLD', 'IN_REPAIR', 'LOST', 'WITHDRAWN'))
);