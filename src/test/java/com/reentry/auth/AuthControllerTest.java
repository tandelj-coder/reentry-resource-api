package com.reentry.auth;

import com.reentry.auth.dto.LoginRequest;
import com.reentry.auth.dto.RefreshTokenRequest;
import com.reentry.auth.dto.RegisterRequest;
import com.reentry.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for /api/auth endpoints — registration, login, and token refresh.
 * These endpoints are public (no JWT required).
 */
@DisplayName("Auth Controller Tests")
class AuthControllerTest extends BaseIntegrationTest {

    // --- Register ---

    @Test
    @DisplayName("POST /api/auth/register — valid CASEWORKER registration returns 201")
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@test.com")
                .password("SecurePass123")
                .role("CASEWORKER")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("jane.doe@test.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register — duplicate email returns 409")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .firstName("Admin")
                .lastName("Dup")
                .email("admin@test.com")   // already seeded in BaseIntegrationTest
                .password("AnyPass123")
                .role("CASEWORKER")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/register — missing fields returns 400")
    void register_missingFields_returns400() throws Exception {
        // No email field
        String badJson = "{\"firstName\":\"X\",\"lastName\":\"Y\",\"password\":\"pass\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    // --- Login ---

    @Test
    @DisplayName("POST /api/auth/login — valid credentials return 200 with token")
    void login_validCredentials_returns200() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("admin@test.com")
                .password("password")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login — wrong password returns 400 or 404")
    void login_wrongPassword_returns4xx() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("admin@test.com")
                .password("WRONG_PASSWORD")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/auth/login — unknown email returns 404")
    void login_unknownEmail_returns404() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("nobody@test.com")
                .password("password")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isNotFound());
    }

    // --- Refresh Token ---

    @Test
    @DisplayName("POST /api/auth/refresh — valid token returns new access token")
    void refreshToken_validToken_returns200() throws Exception {
        // First login to get a token
        LoginRequest login = LoginRequest.builder()
                .email("caseworker@test.com")
                .password("password")
                .build();

        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String existingToken = objectMapper.readTree(responseBody).get("token").asText();

        RefreshTokenRequest refresh = RefreshTokenRequest.builder()
                .token(existingToken)
                .build();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
