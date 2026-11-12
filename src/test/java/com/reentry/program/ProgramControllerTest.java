package com.reentry.program;

import com.reentry.config.BaseIntegrationTest;
import com.reentry.program.dto.CreateProgramRequest;
import com.reentry.provider.dto.CreateServiceProviderRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/programs.
 */
@DisplayName("Program Controller Tests")
class ProgramControllerTest extends BaseIntegrationTest {

    private Long createProvider() throws Exception {
        CreateServiceProviderRequest req = CreateServiceProviderRequest.builder()
                .organizationName("Housing Services")
                .city("Brampton")
                .build();
        String body = mockMvc.perform(post("/api/v1/providers")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/v1/programs — ADMIN creates program, returns 201")
    void createProgram_admin_returns201() throws Exception {
        Long providerId = createProvider();
        CreateProgramRequest req = CreateProgramRequest.builder()
                .providerId(providerId)
                .name("Job Readiness")
                .category(ProgramCategory.EMPLOYMENT)
                .city("Brampton")
                .build();

        mockMvc.perform(post("/api/v1/programs")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Job Readiness"));
    }

    @Test
    @DisplayName("GET /api/v1/programs — CASEWORKER can list all programs")
    void getAllPrograms_caseworker_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/programs")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/programs — PROVIDER can list all programs")
    void getAllPrograms_provider_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/programs")
                        .header("Authorization", providerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/programs — unauthenticated returns 401")
    void getAllPrograms_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/programs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/programs/{id} — unknown id returns 404")
    void getProgram_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/programs/99999")
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/programs — missing name returns 400")
    void createProgram_missingName_returns400() throws Exception {
        Long providerId = createProvider();
        CreateProgramRequest req = CreateProgramRequest.builder()
                .providerId(providerId)
                .category(ProgramCategory.HOUSING)
                .city("Toronto")
                .build();  // name is blank

        mockMvc.perform(post("/api/v1/programs")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isBadRequest());
    }
}
