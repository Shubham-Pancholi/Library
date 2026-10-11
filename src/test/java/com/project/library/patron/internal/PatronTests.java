package com.project.library.patron.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

public class PatronTests {

    @Test 
    public void passedValidPatronValues() {
        PatronType patronType = PatronType.MEMBER;
        String firstName = "Asha";
        String lastName = "Rao";
        String phone = "0101010100";

        Patron patron = new Patron(patronType, firstName, lastName, phone);

        assertThat(patron.getPatronType()).isEqualTo(patronType);
        assertThat(patron.getFirstName()).isEqualTo(firstName);
        assertThat(patron.getLastName()).isEqualTo(lastName);
        assertThat(patron.getPhone()).isEqualTo(phone);
    }

    @Test 
    public void throwExceptionWhenPassedNullPatronType() {

        assertThatThrownBy(() -> new Patron(null, "Asha", "Rao", "0011001100"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void throwExceptionWhenPassedNullFirstName() {
        assertThatThrownBy(() -> new Patron(PatronType.MEMBER, null, null, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void throwExceptionWhenPassedBlankFirstName() {
        assertThatThrownBy(() -> new Patron(PatronType.MEMBER, "", "Rao", "0011001100"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void throwExceptionWhenPassedInvalidFirstName() {
        assertThatThrownBy(() -> new Patron(PatronType.MEMBER, "\t\n", "Rao", "0011001100"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}