package com.project.library.patron.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePatronRequest(
    @NotNull 
    PatronType patronType,

    @NotBlank 
    String firstName,

    String lastName,
    String phone
) {}