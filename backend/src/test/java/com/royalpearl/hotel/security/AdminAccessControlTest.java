package com.royalpearl.hotel.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.royalpearl.hotel.AbstractIntegrationTest;
import com.royalpearl.hotel.booking.repository.BookingRepository;
import com.royalpearl.hotel.user.entity.AppRole;
import com.royalpearl.hotel.user.entity.User;
import com.royalpearl.hotel.user.entity.UserRole;
import com.royalpearl.hotel.user.repository.UserRepository;
import com.royalpearl.hotel.user.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Access-control integration tests:
 *  - Non-admin → GET /admin/** must return 403
 *  - Admin → full access + can set payment status
 *  - Non-admin cannot escalate payment status via /admin
 *  - Admin search by booking reference works exactly
 *  - User can only see own bookings
 */
@DisplayName("Admin Access Control & Security Tests")
class AdminAccessControlTest extends AbstractIntegrationTest {

    @Autowired MockMvc            mockMvc;
    @Autowired ObjectMapper       mapper;
    @Autowired UserRepository     userRepository;
    @Autowired UserRoleRepository userRoleRepository;
    @Autowired BookingRepository  bookingRepository;
    @Autowired PasswordEncoder    passwordEncoder;

    private String adminToken;
    private String userToken;
    private String userEmail;

    // -----------------------------------------------------------------------
    // Setup – create one admin and one plain user before each test
    // -----------------------------------------------------------------------

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        adminToken = createUserAndGetToken("admin-" + UUID.randomUUID() + "@test.com",
                                           AppRole.admin, AppRole.user);
        userEmail  = "user-" + UUID.randomUUID() + "@test.com";
        userToken  = createUserAndGetToken(userEmail, AppRole.user);
    }

    /**
     * Creates a user with given roles, logs them in, returns access token.
     */
    private String createUserAndGetToken(String email, AppRole... roles) throws Exception {
        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Password1!"))
                .fullName("Test " + roles[0].name())
                .provider("email")
                .emailVerified(true)
                .build());

        for (AppRole role : roles) {
            userRoleRepository.save(UserRole.builder().user(user).role(role).build());
        }

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    email,
                        "password", "Password1!"
                ))))
                .andExpect(status().isOk())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        return (String) body.get("accessToken");
    }

    // -----------------------------------------------------------------------
    // 403 for non-admin
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Non-admin calling GET /admin/stats returns 403")
    void nonAdmin_adminStats_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Non-admin calling GET /admin/bookings returns 403")
    void nonAdmin_adminBookings_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/bookings")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Anonymous request to /admin/** returns 403")
    void anonymous_adminEndpoint_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/bookings"))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Admin can access stats
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Admin calling GET /admin/stats returns 200 with stats object")
    void admin_getStats_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/stats")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").isNumber())
                .andExpect(jsonPath("$.totalOrders").isNumber())
                .andExpect(jsonPath("$.revenue").isNumber());
    }

    // -----------------------------------------------------------------------
    // Admin can set payment status; non-admin PATCH on admin endpoint → 403
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Admin can mark booking as paid via PATCH /admin/bookings/{id}")
    void admin_canMarkBookingPaid() throws Exception {
        // Create a guest booking
        MvcResult bookingResult = mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "fullName",  "Pay Test Guest",
                        "email",     "pay-" + UUID.randomUUID() + "@test.com",
                        "phone",     "9111111111",
                        "roomName",  "Deluxe Room",
                        "checkIn",   "2027-04-01",
                        "checkOut",  "2027-04-03",
                        "guests",    1
                ))))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> booking = mapper.readValue(
                bookingResult.getResponse().getContentAsString(), Map.class);
        String bookingUuid = (String) booking.get("id");

        // Admin marks paid
        mockMvc.perform(patch("/api/v1/admin/bookings/" + bookingUuid)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("paymentStatus", "paid"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("paid"));
    }

    @Test
    @DisplayName("Non-admin PATCH on /admin/bookings/{id} returns 403 (cannot set paid)")
    void nonAdmin_cannotSetPaymentStatusViaPatch() throws Exception {
        MvcResult bookingResult = mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "fullName",  "Sneaky Guest",
                        "email",     "sneak-" + UUID.randomUUID() + "@test.com",
                        "phone",     "9222222222",
                        "roomName",  "Deluxe Room",
                        "checkIn",   "2027-05-01",
                        "checkOut",  "2027-05-03",
                        "guests",    1
                ))))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        String bookingUuid = (String) mapper.readValue(
                bookingResult.getResponse().getContentAsString(), Map.class).get("id");

        // Plain user tries to use admin endpoint
        mockMvc.perform(patch("/api/v1/admin/bookings/" + bookingUuid)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("paymentStatus", "paid"))))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------------------------------------
    // Admin search returns exactly the right booking
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Admin search by exact RP reference returns exactly that booking")
    void adminSearch_byExactReference_returnsOneResult() throws Exception {
        String phone = "9333333333";
        MvcResult created = mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "fullName",  "Search Test",
                        "email",     "search-" + UUID.randomUUID() + "@test.com",
                        "phone",     phone,
                        "roomName",  "Executive Suite",
                        "checkIn",   "2027-06-01",
                        "checkOut",  "2027-06-03",
                        "guests",    2
                ))))
                .andExpect(status().isCreated())
                .andReturn();

        @SuppressWarnings("unchecked")
        String ref = (String) mapper.readValue(
                created.getResponse().getContentAsString(), Map.class).get("bookingId");

        mockMvc.perform(get("/api/v1/admin/bookings")
                .header("Authorization", "Bearer " + adminToken)
                .param("search", ref))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].bookingId").value(ref));
    }

    // -----------------------------------------------------------------------
    // User can only see own records
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Authenticated user sees only their own bookings")
    void user_canOnlySeeOwnBookings() throws Exception {
        // Create a booking as the test user (authenticated)
        mockMvc.perform(post("/api/v1/bookings")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "fullName",  "Own Booking",
                        "email",     userEmail,
                        "phone",     "9444444444",
                        "roomName",  "Family Suite",
                        "checkIn",   "2027-07-01",
                        "checkOut",  "2027-07-05",
                        "guests",    3
                ))))
                .andExpect(status().isCreated());

        // Create a booking as a different guest (no token)
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "fullName",  "Other Guest",
                        "email",     "other-" + UUID.randomUUID() + "@test.com",
                        "phone",     "9555555555",
                        "roomName",  "Deluxe Room",
                        "checkIn",   "2027-08-01",
                        "checkOut",  "2027-08-03",
                        "guests",    1
                ))))
                .andExpect(status().isCreated());

        // User's /me/bookings should only include their own
        mockMvc.perform(get("/api/v1/me/bookings")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email",
                        everyItem(equalTo(userEmail))));
    }
}
