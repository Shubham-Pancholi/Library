package com.project.library.circulation.internal;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateBookCopyRequest(
    @NotNull 
    UUID catalogBookId,

    @NotBlank 
    String accessionNumber,

    @Pattern (
        regexp = "(?s).*\\P{javaWhitespace}.*",
        message = "must contain a non-whitespace character"
    )
    String barcode,

    @NotNull 
    CirculationType circulationType
) {}