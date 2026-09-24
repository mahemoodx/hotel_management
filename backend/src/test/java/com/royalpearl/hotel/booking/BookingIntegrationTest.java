package com.royalpearl.hotel.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.royalpearl.hotel.AbstractIntegrationTest;
import com.royalpearl.hotel.booking.repository.BookingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for guest booking creation, reference uniqueness,
 * payment-status protection, and lookup endpoint.
 */
@DisplayName("Booking Integration Tests")
class BookingIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc            mockMvc;
    @Autowired ObjectMapper       mapper;
    @Autowired BookingRepository  bookingRepository;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Map<String, Object> buildBookingPayload(String email, String phone) {
        return Map.of(
                "fullName",      "Guest Traveller",
                "email",         email,
                "phone",         phone,
                "roomName",      "Deluxe Room",
                "checkIn",       "2027-03-01",
                "checkOut",      "2027-03-04",
                "guests",        2,
                "paymentMethod", "cash"
        );
    }

    private MvcResult createGuestBooking(String email, String phone) throws Exception {
        return mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(buildBookingPayload(email, phone))))
                .andExpect(status().isCreated())
                .andReturn();
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Anonymous POST /bookings returns 201 with RP…reference")
    void guestBooking_createsWithRpReference() throws Exception {
        String email = "guest-" + UUID.randomUUID() + "@test.com";
        String phone = "9" + (1000000000L + (long)(Math.random() * 1000000000));

        MvcResult result = createGuestBooking(email, phone);

        String body = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> response = mapper.readValue(body, Map.class);

        String bookingId = (String) response.get("bookingId");
        assertThat(bookingId).isNotNull();
        assertThat(bookingId).matches("RP\\d{7}");
    }

    @Test
    @DisplayName("Booking reference is persisted in the database")
    void guestBooking_referencePersistedInDb() throws Exception {
        String email = "persist-" + UUID.randomUUID() + "@test.com";
        String phone = "8" + (1000000000L + (long)(Math.random() * 1000000000));

        MvcResult result = createGuestBooking(email, phone);
        String body = result.getResponse().getContentAsString();
        @SuppressWarnings("unchecked")
        Map<String, Object> response = mapper.readValue(body, Map.class);
        String bookingId = (String) response.get("bookingId");

        // Verify DB persistence
        assertThat(bookingRepository.findByBookingId(bookingId)).isPresent();
    }

    @Test
    @DisplayName("Two simultaneous bookings get different RP references")
    void guestBooking_twoBookingsHaveUniqueRefs() throws Exception {
        String phone1 = "91" + (10000000L + (long)(Math.random() * 10000000));
        String phone2 = "92" + (10000000L + (long)(Math.random() * 10000000));

        MvcResult r1 = createGuestBooking("a-" + UUID.randomUUID() + "@x.com", phone1);
        MvcResult r2 = createGuestBooking("b-" + UUID.randomUUID() + "@x.com", phone2);

        @SuppressWarnings("unchecked")
        String ref1 = (String) mapper.readValue(r1.getResponse().getContentAsString(), Map.class)
                .get("bookingId");
        @SuppressWarnings("unchecked")
        String ref2 = (String) mapper.readValue(r2.getResponse().getContentAsString(), Map.class)
                .get("bookingId");

        assertThat(ref1).isNotEqualTo(ref2);
    }

    @Test
    @DisplayName("Payment status is always 'pending' on creation regardless of client input")
    void guestBooking_paymentStatusAlwaysPending() throws Exception {
        // Try to sneak in paymentStatus=paid
        Map<String, Object> payload = new java.util.HashMap<>(buildBookingPayload(
                "evil-" + UUID.randomUUID() + "@test.com",
                "70" + (10000000L + (long)(Math.random() * 10000000))
        ));
        payload.put("paymentStatus", "paid");   // client attempts escalation

        MvcResult result = mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = mapper.readValue(
                result.getResponse().getContentAsString(), Map.class);

        // Server must ignore client-supplied paymentStatus
        assertThat(response.get("paymentStatus")).isEqualTo("pending");
    }

    @Test
    @DisplayName("Booking validation: missing fullName returns 422")
    void booking_missingFullName_returns422() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",   "x@test.com",
                        "phone",   "9876543210",
                        "guests",  1
                ))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.fullName").exists());
    }

    @Test
    @DisplayName("Lookup by reference+phone returns booking status")
    void lookup_validRefAndPhone_returnsBooking() throws Exception {
        String email = "lookup-" + UUID.randomUUID() + "@test.com";
        String phone = "9900000001";

        MvcResult created = createGuestBooking(email, phone);
        @SuppressWarnings("unchecked")
        String ref = (String) mapper.readValue(
                created.getResponse().getContentAsString(), Map.class).get("bookingId");

        mockMvc.perform(get("/api/v1/bookings/lookup")
                .param("reference", ref)
                .param("phone",     phone))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(ref))
                .andExpect(jsonPath("$.paymentStatus").value("pending"));
    }

    @Test
    @DisplayName("Lookup with wrong phone returns 404")
    void lookup_wrongPhone_returns404() throws Exception {
        String email = "lookup2-" + UUID.randomUUID() + "@test.com";
        String phone = "9900000002";

        MvcResult created = createGuestBooking(email, phone);
        @SuppressWarnings("unchecked")
        String ref = (String) mapper.readValue(
                created.getResponse().getContentAsString(), Map.class).get("bookingId");

        mockMvc.perform(get("/api/v1/bookings/lookup")
                .param("reference", ref)
                .param("phone",     "0000000000"))
                .andExpect(status().isNotFound());
    }
}
