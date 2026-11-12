package com.reentry.referral;

import com.reentry.client.dto.CreateClientRequest;
import com.reentry.config.BaseIntegrationTest;
import com.reentry.program.ProgramCategory;
import com.reentry.program.dto.CreateProgramRequest;
import com.reentry.provider.dto.CreateServiceProviderRequest;
import com.reentry.referral.dto.CreateReferralRequest;
import com.reentry.referral.dto.UpdateReferralStatusRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/referrals — access control and data integrity.
 */
@DisplayName("Referral Controller Tests")
class ReferralControllerTest extends BaseIntegrationTest {

    private Long createClient() throws Exception {
        CreateClientRequest req = CreateClientRequest.builder()
                .firstName("Ref")
                .lastName("Client")
                .build();
        String body = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createProvider() throws Exception {
        CreateServiceProviderRequest req = CreateServiceProviderRequest.builder()
                .organizationName("Test Provider")
                .city("Toronto")
                .build();
        String body = mockMvc.perform(post("/api/v1/providers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private Long createProgram(Long providerId) throws Exception {
        CreateProgramRequest req = CreateProgramRequest.builder()
                .providerId(providerId)
                .name("Test Program")
                .category(ProgramCategory.EMPLOYMENT)
                .city("Toronto")
                .build();
        String body = mockMvc.perform(post("/api/v1/programs")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/v1/referrals — CASEWORKER creates referral, returns 201")
    void createReferral_caseworker_returns201() throws Exception {
        Long clientId = createClient();
        Long providerId = createProvider();
        Long programId = createProgram(providerId);

        CreateReferralRequest req = CreateReferralRequest.builder()
                .clientId(clientId)
                .programId(programId)
                .notes("Priority case")
                .build();

        mockMvc.perform(post("/api/v1/referrals")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/referrals — missing clientId returns 400")
    void createReferral_missingClientId_returns400() throws Exception {
        String badBody = "{\"programId\":1,\"notes\":\"test\"}";

        mockMvc.perform(post("/api/v1/referrals")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/referrals/pending — CASEWORKER returns 200")
    void getPendingReferrals_caseworker_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/referrals/pending")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/referrals/pending — ADMIN returns 200")
    void getPendingReferrals_admin_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/referrals/pending")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/referrals/{id} — PROVIDER can read referral (authorized)")
    void getReferral_provider_returns200OrNotFound() throws Exception {
        // PROVIDER is authorized for GET by ID
        mockMvc.perform(get("/api/v1/referrals/99999")
                        .header("Authorization", providerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/referrals/client/{clientId} — CASEWORKER returns 200 with empty list")
    void getReferralsByClient_caseworker_returns200() throws Exception {
        Long clientId = createClient();
        mockMvc.perform(get("/api/v1/referrals/client/" + clientId)
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("PUT /api/v1/referrals/{id}/status — unknown referral returns 404")
    void updateStatus_unknownReferral_returns404() throws Exception {
        UpdateReferralStatusRequest req = UpdateReferralStatusRequest.builder()
                .newStatus("ACCEPTED")
                .build();

        mockMvc.perform(put("/api/v1/referrals/99999/status")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isNotFound());
    }
}
