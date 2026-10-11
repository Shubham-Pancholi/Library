package com.project.library.catalog.internal;

import jakarta.validation.constraints.NotBlank;

public record CreateBookRequest(
    @NotBlank 
    String title
) {}