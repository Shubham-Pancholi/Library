package com.project.library.patron.internal;

import jakarta.validation.constraints.NotBlank;

public record AdministrativeBlockActionRequest(
    @NotBlank String reason
) {}