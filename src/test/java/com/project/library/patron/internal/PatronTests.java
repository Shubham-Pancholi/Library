package com.project.library.patron.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Test 
    public void passedValidBlockingValues() {
        String reason = "Testing";

        Patron patron = new Patron(PatronType.MEMBER, "FName", "LName", "0");

        assertThat(patron.isAdministrativelyBlocked()).isFalse();
        assertThat(patron.getAdministrativeBlockReason()).isNull();

        patron.blockAdministratively(reason);

        assertThat(patron.isAdministrativelyBlocked()).isTrue();
        assertThat(patron.getAdministrativeBlockReason()).isNotNull().isEqualTo(reason);

        patron.clearAdministrativeBlock();

        assertThat(patron.isAdministrativelyBlocked()).isFalse();
        assertThat(patron.getAdministrativeBlockReason()).isNull();
    }

    @ParameterizedTest 
    @NullAndEmptySource 
    @ValueSource (strings = {" ","\t\n"})
    public void passedInvalidBlockingReasonsThrows(String reason) {
        Patron patron = new Patron(PatronType.MEMBER, "FName", "LName", "0");

        assertThatThrownBy(() -> patron.blockAdministratively(reason))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(patron.isAdministrativelyBlocked()).isFalse();
        assertThat(patron.getAdministrativeBlockReason()).isNull();
    }

    @Test 
    public void invalidBlockingOperationsThrows() {
        Patron patron = new Patron(PatronType.MEMBER, "FName", "LName", "0");

        assertThatThrownBy(() -> patron.clearAdministrativeBlock())
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(patron.isAdministrativelyBlocked()).isFalse();
        assertThat(patron.getAdministrativeBlockReason()).isNull();
        
        String reason1 = "Testing";
        String reason2 = "Error Handling";

        patron.blockAdministratively(reason1);

        assertThatThrownBy(() -> patron.blockAdministratively(reason2))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(patron.isAdministrativelyBlocked()).isTrue();
        assertThat(patron.getAdministrativeBlockReason()).isEqualTo(reason1);
    }
}