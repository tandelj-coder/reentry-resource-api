package com.reentry.audit;

import com.reentry.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for /api/v1/audit-logs — ADMIN-only access.
 * Core assertion: non-ADMIN callers get 403, never 500.
 */
@DisplayName("Audit Log Controller Tests")
class AuditLogControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("GET /api/v1/audit-logs/user/{id} — ADMIN returns 200")
    void getByUser_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/user/" + adminUser.getId())
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/user/{id} — CASEWORKER returns 403")
    void getByUser_caseworker_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/user/1")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/user/{id} — PROVIDER returns 403")
    void getByUser_provider_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/user/1")
                        .header("Authorization", providerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/action/{action} — ADMIN returns 200")
    void getByAction_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/action/CREATE")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/action/{action} — CASEWORKER returns 403 (not 500)")
    void getByAction_caseworker_returns403NotInternalServerError() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/action/CREATE")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/entity/{id} — ADMIN returns 200")
    void getByEntityId_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/entity/1")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/entity-type/{type} — ADMIN returns 200")
    void getByEntityType_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/entity-type/Client")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/audit-logs/history/{type}/{id} — ADMIN returns 200")
    void getHistory_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/history/Client/1")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }
}
