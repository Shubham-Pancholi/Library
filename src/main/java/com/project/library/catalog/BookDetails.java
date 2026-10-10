package com.project.library.catalog;

import java.util.UUID;

public record BookDetails(
    UUID id,
    String title
) {}