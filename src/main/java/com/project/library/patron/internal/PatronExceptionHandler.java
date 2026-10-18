package com.project.library.patron.internal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice (assignableTypes = StaffPatronController.class)
public class PatronExceptionHandler {

    @ExceptionHandler (PatronNotFoundException.class)
    public ProblemDetail handlePatronNotFound(PatronNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler (AdministrativeBlockConflictException.class)
    public ProblemDetail handleAdministrativeBlockConflict(AdministrativeBlockConflictException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}