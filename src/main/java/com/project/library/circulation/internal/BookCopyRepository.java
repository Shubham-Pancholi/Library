package com.project.library.circulation.internal;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends  JpaRepository<BookCopy, UUID>{
}