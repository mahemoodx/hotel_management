package com.royalpearl.hotel.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.royalpearl.hotel.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the full authentication flow:
 *   register → login → refresh → logout
 */
@DisplayName("Auth Flow Integration Tests")
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper mapper;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    private Map<String, String> registerAndLogin(String email) throws Exception {
        // Register
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    email,
                        "password", "Password123!",
                        "fullName", "Test User"
                ))))
                .andExpect(status().isCreated());

        // Login
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    email,
                        "password", "Password123!"
                ))))
                .andExpect(status().isOk())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        return Map.of(
                "accessToken",  (String) body.get("accessToken"),
                "refreshToken", (String) body.get("refreshToken")
        );
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Register with valid payload returns 201 with tokens")
    void register_validPayload_returns201WithTokens() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    uniqueEmail(),
                        "password", "Password123!",
                        "fullName", "Alice Hyderabad"
                ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.roles", hasItem("user")));
    }

    @Test
    @DisplayName("Register with duplicate email returns 409")
    void register_duplicateEmail_returns409() throws Exception {
        String email = uniqueEmail();
        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    email,
                        "password", "Password123!",
                        "fullName", "First User"
                ))))
                .andExpect(status().isCreated());

        // Duplicate
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    email,
                        "password", "Password123!",
                        "fullName", "Second User"
                ))))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    @DisplayName("Register with blank email returns 422 validation error")
    void register_blankEmail_returns422() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    "",
                        "password", "Password123!",
                        "fullName", "Test"
                ))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("Login with wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    email,
                        "password", "Password123!",
                        "fullName", "Test User"
                ))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of(
                        "email",    email,
                        "password", "WrongPassword!"
                ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh token rotates and issues new access token")
    void refresh_validToken_issuesNewPair() throws Exception {
        String email = uniqueEmail();
        Map<String, String> tokens = registerAndLogin(email);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(
                        Map.of("refreshToken", tokens.get("refreshToken"))
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Using a refresh token twice (after rotation) returns 400")
    void refresh_usedTwice_returns400() throws Exception {
        String email = uniqueEmail();
        Map<String, String> tokens = registerAndLogin(email);
        String rawRefresh = tokens.get("refreshToken");

        // First use – rotate
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("refreshToken", rawRefresh))))
                .andExpect(status().isOk());

        // Second use of same token – should fail
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(Map.of("refreshToken", rawRefresh))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Accessing /me without token returns 403")
    void me_noToken_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Accessing /me with valid token returns user profile")
    void me_withValidToken_returnsProfile() throws Exception {
        String email = uniqueEmail();
        Map<String, String> tokens = registerAndLogin(email);

        mockMvc.perform(get("/api/v1/me")
                .header("Authorization", "Bearer " + tokens.get("accessToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.roles", hasItem("user")));
    }
}
