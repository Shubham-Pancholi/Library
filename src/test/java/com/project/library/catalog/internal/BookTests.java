package com.project.library.catalog.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

public class BookTests {

    @Test 
    public void passedValidBookTitle() {
        String title = "The Hobbit";

        assertThat(new Book(title).getTitle()).isEqualTo(title);
    }

    @Test 
    public void throwExceptionWhenPassedNull() {
        assertThatThrownBy(() -> new Book(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void throwExceptionWhenPassedBlankTitle() {
        assertThatThrownBy(() -> new Book(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test 
    public void rejectWhitespaceOnlyTitle() {
        assertThatThrownBy(() -> new Book("\t\n"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}