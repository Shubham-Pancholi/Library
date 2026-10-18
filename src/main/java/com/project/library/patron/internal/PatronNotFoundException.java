package com.project.library.patron.internal;

public class PatronNotFoundException extends RuntimeException {
    
    public PatronNotFoundException(String message) {
        super(message);
    }
}