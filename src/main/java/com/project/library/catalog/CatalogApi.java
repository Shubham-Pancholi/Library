package com.project.library.catalog;

import java.util.Optional;
import java.util.UUID;

public interface CatalogApi {

    Optional<BookDetails> findBook(UUID bookId);
}
