package com.reentry.client;

import com.reentry.client.dto.CreateClientRequest;
import com.reentry.client.dto.UpdateClientRequest;
import com.reentry.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/clients — CRUD operations and data integrity.
 */
@DisplayName("Client Controller Tests")
class ClientControllerTest extends BaseIntegrationTest {

    // ─── Create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/clients — CASEWORKER creates client, returns 201 with id")
    void createClient_caseworker_returns201() throws Exception {
        CreateClientRequest req = CreateClientRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .birthYear(1990)
                .city("Toronto")
                .build();

        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @DisplayName("POST /api/v1/clients — ADMIN creates client, returns 201")
    void createClient_admin_returns201() throws Exception {
        CreateClientRequest req = CreateClientRequest.builder()
                .firstName("Alice")
                .lastName("Jones")
                .build();

        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/clients — missing firstName returns 400")
    void createClient_missingFirstName_returns400() throws Exception {
        String badBody = "{\"lastName\":\"Smith\"}";

        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest());
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/clients — CASEWORKER gets empty list when no clients exist")
    void getAllClients_empty_returns200WithEmptyArray() throws Exception {
        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/clients — CASEWORKER sees created client in list")
    void getAllClients_afterCreate_containsClient() throws Exception {
        CreateClientRequest req = CreateClientRequest.builder()
                .firstName("Bob")
                .lastName("Brown")
                .build();
        mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)));

        mockMvc.perform(get("/api/v1/clients")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Bob"));
    }

    @Test
    @DisplayName("GET /api/v1/clients/{id} — unknown id returns 404")
    void getClientById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/clients/99999")
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/clients/{id} — created client is retrievable")
    void getClientById_existingClient_returns200() throws Exception {
        CreateClientRequest req = CreateClientRequest.builder()
                .firstName("Carol")
                .lastName("White")
                .build();

        String createBody = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createBody).get("id").asLong();

        mockMvc.perform(get("/api/v1/clients/" + id)
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Carol"));
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/v1/clients/{id} — CASEWORKER updates existing client")
    void updateClient_caseworker_returns200() throws Exception {
        CreateClientRequest create = CreateClientRequest.builder()
                .firstName("Dan")
                .lastName("Green")
                .build();

        String createBody = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(create)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createBody).get("id").asLong();

        UpdateClientRequest update = UpdateClientRequest.builder()
                .firstName("Daniel")
                .lastName("Green")
                .build();

        mockMvc.perform(put("/api/v1/clients/" + id)
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Daniel"));
    }

    // ─── Delete (deactivate) ──────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/v1/clients/{id} — ADMIN deactivates client, returns 204")
    void deactivateClient_admin_returns204() throws Exception {
        CreateClientRequest create = CreateClientRequest.builder()
                .firstName("Eve")
                .lastName("Black")
                .build();

        String createBody = mockMvc.perform(post("/api/v1/clients")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(create)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(createBody).get("id").asLong();

        mockMvc.perform(delete("/api/v1/clients/" + id)
                        .header("Authorization", adminToken))
                .andExpect(status().isNoContent());
    }
}
