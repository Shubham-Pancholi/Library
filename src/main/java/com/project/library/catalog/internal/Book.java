package com.project.library.catalog.internal;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "book", schema = "catalog")
public class Book {

    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column (nullable = false, columnDefinition = "text")
    private String title;

    protected Book() {
    }

    public Book(String aTitle) {
        if (aTitle == null || aTitle.isBlank()) {
            throw new IllegalArgumentException("Invalid title value.");
        }
        title = aTitle;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}