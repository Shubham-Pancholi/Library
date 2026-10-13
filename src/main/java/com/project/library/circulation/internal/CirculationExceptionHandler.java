package com.project.library.circulation.internal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice (
    assignableTypes = StaffBookCopyController.class
)
public class CirculationExceptionHandler {

    @ExceptionHandler (CatalogBookNotFoundException.class)
    public ProblemDetail handleCatalogBookNotFound(
        CatalogBookNotFoundException exception
    ) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }
}