package com.royalpearl.hotel.common;

import java.security.SecureRandom;

/**
 * Generates human-readable references that are unique by combining
 * a random 7-digit number.
 *
 * Booking  → RP  + 7 digits  e.g. RP4820193
 * Order    → RPO-+ 7 digits  e.g. RPO-7710284
 */
public final class ReferenceGenerator {

    private static final SecureRandom RNG = new SecureRandom();
    private static final int MIN = 1_000_000;
    private static final int MAX = 9_999_999;

    private ReferenceGenerator() {}

    /** Returns a reference like "RP4820193". */
    public static String bookingRef() {
        return "RP" + randomDigits();
    }

    /** Returns a reference like "RPO-7710284". */
    public static String orderRef() {
        return "RPO-" + randomDigits();
    }

    private static int randomDigits() {
        return MIN + RNG.nextInt(MAX - MIN + 1);
    }
}
