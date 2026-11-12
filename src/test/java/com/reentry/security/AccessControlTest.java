package com.reentry.security;

import com.reentry.client.dto.CreateClientRequest;
import com.reentry.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Access-control regression tests — the core security finding documented in FINDINGS.md.
 *
 * Before the fix, @PreAuthorize denials fell through the global exception handler's
 * catch-all and returned 500 Internal Server Error instead of 403 Forbidden.
 * Every test here asserts that an unauthorized caller gets exactly 403, not 500.
 *
 * Access matrix:
 *   POST /api/v1/clients          → CASEWORKER, ADMIN only  (PROVIDER → 403)
 *   GET  /api/v1/clients          → CASEWORKER, ADMIN only  (PROVIDER → 403)
 *   GET  /api/v1/clients/{id}     → CASEWORKER, ADMIN, PROVIDER (unauthenticated → 401)
 *   DELETE /api/v1/clients/{id}   → CASEWORKER, ADMIN only  (PROVIDER → 403)
 *   GET /api/v1/audit-logs/...    → ADMIN only              (CASEWORKER, PROVIDER → 403)
 *   GET /api/v1/referrals/pending → CASEWORKER, ADMIN only  (PROVIDER → 403)
 *   Any protected endpoint        → unauthenticated → 401
 */
@DisplayName("Access Control — @PreAuthorize returns 403 (not 500)")
class AccessControlTest extends BaseIntegrationTest {

    private CreateClientRequest validClientRequest() {
        return CreateClientRequest.builder()
                .firstName("Test")
                .lastName("Client")
                .build();
    }

    // ─── Unauthenticated requests return 401 ─────────────────────────────────

    @Test
    @DisplayName("No token on protected endpoint → 401 Unauthorized")
    void noToken_protectedEndpoint_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("No token on POST clients → 401 Unauthorized")
    void noToken_postClients_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(validClientRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("No token on audit logs → 401 Unauthorized")
    void noToken_auditLogs_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/user/1"))
                .andExpect(status().isUnauthorized());
    }

    // ─── PROVIDER is blocked from CASEWORKER-only endpoints (must be 403, not 500) ─

    @Test
    @DisplayName("PROVIDER POST /api/v1/clients → 403 Forbidden (not 500)")
    void provider_postClients_returns403NotInternalServerError() throws Exception {
        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(validClientRequest())))
                .andExpect(status().isForbidden());   // 403, never 500
    }

    @Test
    @DisplayName("PROVIDER GET /api/v1/clients → 403 Forbidden (not 500)")
    void provider_getAllClients_returns403NotInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", providerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROVIDER DELETE /api/v1/clients/1 → 403 Forbidden (not 500)")
    void provider_deleteClient_returns403NotInternalServerError() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/1")
                        .header("Authorization", providerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROVIDER GET /api/v1/referrals/pending → 403 Forbidden (not 500)")
    void provider_getPendingReferrals_returns403NotInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/referrals/pending")
                        .header("Authorization", providerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROVIDER POST /api/v1/referrals → 403 Forbidden (not 500)")
    void provider_postReferral_returns403NotInternalServerError() throws Exception {
        String body = "{\"clientId\":1,\"programId\":1,\"notes\":\"test\"}";
        mockMvc.perform(post("/api/v1/referrals")
                        .header("Authorization", providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    // ─── CASEWORKER is blocked from ADMIN-only endpoints ────────────────────

    @Test
    @DisplayName("CASEWORKER GET /api/v1/audit-logs → 403 Forbidden (not 500)")
    void caseworker_auditLogs_returns403NotInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/user/1")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("CASEWORKER GET /api/v1/audit-logs by action → 403 Forbidden (not 500)")
    void caseworker_auditLogsByAction_returns403NotInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/action/CREATE")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PROVIDER GET /api/v1/audit-logs → 403 Forbidden (not 500)")
    void provider_auditLogs_returns403NotInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/entity/1")
                        .header("Authorization", providerToken))
                .andExpect(status().isForbidden());
    }

    // ─── Authorized roles are still allowed ──────────────────────────────────

    @Test
    @DisplayName("ADMIN GET /api/v1/clients → 200 OK (authorized)")
    void admin_getAllClients_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CASEWORKER GET /api/v1/clients → 200 OK (authorized)")
    void caseworker_getAllClients_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PROVIDER GET /api/v1/clients/{id} → 200 or 404 (never 403)")
    void provider_getClientById_notForbidden() throws Exception {
        // PROVIDER is authorized for GET by ID; result is 404 since no client created
        mockMvc.perform(get("/api/v1/clients/9999")
                        .header("Authorization", providerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Public endpoint /api/auth/login → 400 (bad body) with no token")
    void publicEndpoint_noToken_notBlocked() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
