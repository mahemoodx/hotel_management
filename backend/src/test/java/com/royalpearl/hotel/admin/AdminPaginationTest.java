package com.royalpearl.hotel.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.royalpearl.hotel.AbstractIntegrationTest;
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

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests that admin list endpoints honour page/size/sort parameters
 * and return proper Spring Page metadata.
 */
@DisplayName("Admin Pagination Tests")
class AdminPaginationTest extends AbstractIntegrationTest {

    @Autowired MockMvc            mockMvc;
    @Autowired ObjectMapper       mapper;
    @Autowired UserRepository     userRepository;
    @Autowired UserRoleRepository userRoleRepository;
    @Autowired PasswordEncoder    passwordEncoder;

    private String adminToken;

    @BeforeEach
    @Transactional
    void setUp() throws Exception {
        String email = "admin-pag-" + UUID.randomUUID() + "@test.com";
        User admin = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("AdminPass1!"))
                .fullName("Admin Pagination")
                .provider("email")
                .emailVerified(true)
                .build());
        userRoleRepository.save(UserRole.builder().user(admin).role(AppRole.admin).build());
        userRoleRepository.save(UserRole.builder().user(admin).role(AppRole.user).build());

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    email,
                        "password", "AdminPass1!"
                ))))
                .andExpect(status().isOk())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        adminToken = (String) body.get("accessToken");

        // Seed 5 bookings
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(Map.of(
                            "fullName",  "Seed Guest " + i,
                            "email",     "seed" + i + "@test.com",
                            "phone",     "90000000" + (10 + i),
                            "roomName",  "Deluxe Room",
                            "checkIn",   "2027-09-01",
                            "checkOut",  "2027-09-03",
                            "guests",    1
                    ))))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @DisplayName("GET /admin/bookings returns paginated response with Spring Page structure")
    void adminBookings_returnsPageStructure() throws Exception {
        mockMvc.perform(get("/api/v1/admin/bookings")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(2))))
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber());
    }

    @Test
    @DisplayName("GET /admin/bookings page size is respected")
    void adminBookings_pageSizeRespected() throws Exception {
        mockMvc.perform(get("/api/v1/admin/bookings")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(3))))
                .andExpect(jsonPath("$.size").value(3));
    }

    @Test
    @DisplayName("GET /admin/orders returns paginated response")
    void adminOrders_returnsPageStructure() throws Exception {
        // Seed one order
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "name",  "Page Test",
                        "phone", "9000009999",
                        "items", java.util.List.of(
                                Map.of("name", "Biryani", "price", 350, "qty", 1)
                        ),
                        "total", 350
                ))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/admin/orders")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @DisplayName("GET /admin/users returns paginated user list")
    void adminUsers_returnsPageStructure() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", "Bearer " + adminToken)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }
}
