package com.project.library.circulation.internal;

import java.util.UUID;

public record BookCopyDetails(
    UUID id,
    UUID catalogBookId,
    String accessionNumber,
    String barcode,
    CirculationType circulationType,
    CopyAvailabilityStatus availabilityStatus
) {}