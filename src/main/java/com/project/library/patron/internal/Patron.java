package com.project.library.patron.internal;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "patron", schema = "patron")
public class Patron {

    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated (EnumType.STRING)
    @Column (name = "patron_type", nullable = false, length = 16)
    private PatronType patronType;

    @Column (name = "first_name", nullable = false, columnDefinition = "text")
    private String firstName;

    @Column (name = "last_name", columnDefinition = "text")
    private String lastName;

    @Column (columnDefinition = "text")
    private String phone;

    @Column (name = "administrative_block", nullable = false)
    private boolean administrativeBlock;

    @Column (name = "administrative_block_reason", columnDefinition = "text", nullable = true)
    private String administrativeBlockReason;

    protected Patron() {
    }

    public Patron(
        PatronType aPatronType,
        String aFirstName,
        String aLastName,
        String aPhone
    ) {
        if (aPatronType == null || aFirstName == null || aFirstName.isBlank()) {
            throw new IllegalArgumentException("Invalid Patron fields.");
        }

        patronType = aPatronType;
        firstName = aFirstName;
        lastName = aLastName;
        phone = aPhone;
    }

    public UUID getId() {
        return id;
    }

    public PatronType getPatronType() {
        return patronType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isAdministrativelyBlocked() {
        return administrativeBlock;
    }

    public String getAdministrativeBlockReason() {
        return administrativeBlockReason;
    }

    public void blockAdministratively(String reason) {
        if (isAdministrativelyBlocked()) {
            throw new IllegalArgumentException("Invalid Blocking operation: patron already blocked");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Invalid block reason value.");
        }

        administrativeBlock = true;
        administrativeBlockReason = reason;
    }

    public void clearAdministrativeBlock() {
        if (!isAdministrativelyBlocked()) {
            throw new IllegalArgumentException("Invalid Blocking operation: patron is not blocked");
        }

        administrativeBlock = false;
        administrativeBlockReason = null;
    }
}