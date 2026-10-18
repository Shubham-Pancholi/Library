package com.project.library.patron.internal;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface PatronRepository extends JpaRepository<Patron, UUID> {
    
    @Query ("SELECT p FROM Patron p WHERE p.id = :id")
    @Lock (LockModeType.PESSIMISTIC_WRITE)
    public Optional<Patron> findByIdForUpdate(@Param("id") UUID id);
}