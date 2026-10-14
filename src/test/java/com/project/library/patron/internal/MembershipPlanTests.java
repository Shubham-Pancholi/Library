package com.project.library.patron.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class MembershipPlanTests {

    @Test 
    public void retainsValidValues() {
        String name = "validValues";

        MembershipPlan mp = new MembershipPlan(name);

        assertThat(mp).isNotNull();
        assertThat(mp.getName()).isEqualTo(name);
    }

    @ParameterizedTest 
    @NullAndEmptySource 
    @ValueSource (strings = {" ", " \t\n"})
    public void invalidNamesAreRejected(String  name) {
        assertThatThrownBy(() -> new MembershipPlan(name))
            .isInstanceOf(IllegalArgumentException.class);
    }
}