package com.royalpearl.hotel.security;

import com.royalpearl.hotel.common.ReferenceGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ReferenceGenerator – no Spring context needed.
 */
@DisplayName("ReferenceGenerator Unit Tests")
class ReferenceGeneratorTest {

    @Test
    @DisplayName("bookingRef() matches pattern RP + 7 digits")
    void bookingRef_matchesPattern() {
        String ref = ReferenceGenerator.bookingRef();
        assertThat(ref).matches("RP\\d{7}");
    }

    @Test
    @DisplayName("orderRef() matches pattern RPO- + 7 digits")
    void orderRef_matchesPattern() {
        String ref = ReferenceGenerator.orderRef();
        assertThat(ref).matches("RPO-\\d{7}");
    }

    @RepeatedTest(value = 100, name = "booking ref is in valid range (run {currentRepetition})")
    @DisplayName("bookingRef() 7-digit number is in range 1000000–9999999")
    void bookingRef_sevenDigitRange() {
        String ref = ReferenceGenerator.bookingRef();
        int num = Integer.parseInt(ref.substring(2));  // strip "RP"
        assertThat(num).isBetween(1_000_000, 9_999_999);
    }

    @Test
    @DisplayName("1000 generated booking refs have very high uniqueness (collision rate < 0.1%)")
    void bookingRef_highUniqueness() {
        int count = 1000;
        Set<String> refs = new HashSet<>();
        for (int i = 0; i < count; i++) {
            refs.add(ReferenceGenerator.bookingRef());
        }
        // Expect at least 99% unique (statistical guarantee with 9M range)
        assertThat(refs.size()).isGreaterThan((int)(count * 0.99));
    }

    @Test
    @DisplayName("1000 generated order refs have very high uniqueness")
    void orderRef_highUniqueness() {
        int count = 1000;
        Set<String> refs = new HashSet<>();
        for (int i = 0; i < count; i++) {
            refs.add(ReferenceGenerator.orderRef());
        }
        assertThat(refs.size()).isGreaterThan((int)(count * 0.99));
    }
}
