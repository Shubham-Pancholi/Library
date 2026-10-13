package com.project.library.circulation.internal;

import java.util.UUID;

public class CatalogBookNotFoundException extends RuntimeException {

    public CatalogBookNotFoundException(UUID id) {
        super("Catalog book not found: " + id);
    }
}