package com.project.library.patron.internal;

import java.util.UUID;

public record PatronDetails(
    UUID id,
    PatronType patronType,
    String firstName,
    String lastName,
    String phone
) {}