package com.project.library.circulation.internal;

public class DuplicateCopyIdentifierException extends RuntimeException {

    public DuplicateCopyIdentifierException(String identifier, Throwable cause) {
        super(identifier + " is already in use", cause);
    }
}