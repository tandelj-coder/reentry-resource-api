package com.reentry.provider;

import com.reentry.config.BaseIntegrationTest;
import com.reentry.provider.dto.CreateServiceProviderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/providers.
 */
@DisplayName("Service Provider Controller Tests")
class ServiceProviderControllerTest extends BaseIntegrationTest {

    private CreateServiceProviderRequest validRequest() {
        return CreateServiceProviderRequest.builder()
                .organizationName("Reintegration House")
                .city("Mississauga")
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/providers — ADMIN creates provider, returns 201")
    void createProvider_admin_returns201() throws Exception {
        mockMvc.perform(post("/api/v1/providers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.organizationName").value("Reintegration House"));
    }

    @Test
    @DisplayName("GET /api/v1/providers — any authenticated user can list providers")
    void getAllProviders_caseworker_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/providers")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/providers — unauthenticated returns 401")
    void getAllProviders_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/providers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/providers — missing organizationName returns 400")
    void createProvider_missingOrganizationName_returns400() throws Exception {
        String badBody = "{\"city\":\"Toronto\"}";
        mockMvc.perform(post("/api/v1/providers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/providers/{id} — created provider is retrievable")
    void getProvider_existingProvider_returns200() throws Exception {
        String createBody = mockMvc.perform(post("/api/v1/providers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(validRequest())))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createBody).get("id").asLong();

        mockMvc.perform(get("/api/v1/providers/" + id)
                        .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationName").value("Reintegration House"));
    }

    @Test
    @DisplayName("GET /api/v1/providers/{id} — unknown id returns 404")
    void getProvider_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/providers/99999")
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }
}
