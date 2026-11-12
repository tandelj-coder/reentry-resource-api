package com.reentry.followup;

import com.reentry.config.BaseIntegrationTest;
import com.reentry.followup.dto.CreateFollowUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for /api/v1/followups.
 */
@DisplayName("Follow-Up Controller Tests")
class FollowUpControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("POST /api/v1/follow-ups — missing referralId returns 400")
    void createFollowUp_missingReferralId_returns400() throws Exception {
        String badBody = "{\"type\":\"CHECK_IN\",\"scheduledDate\":\"2026-12-01T10:00:00\"}";
        mockMvc.perform(post("/api/v1/follow-ups")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/followups — unknown referralId returns 404")
    void createFollowUp_unknownReferral_returns404() throws Exception {
        CreateFollowUpRequest req = CreateFollowUpRequest.builder()
                .referralId(99999L)
                .type(FollowUpType.CHECK_IN)
                .scheduledDate(LocalDateTime.now().plusDays(7))
                .notes("Check in call")
                .build();

        mockMvc.perform(post("/api/v1/follow-ups")
                        .header("Authorization", caseworkerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/follow-ups/pending — unauthenticated returns 401")
    void getAllFollowUps_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/follow-ups/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/follow-ups/pending — CASEWORKER returns 200")
    void getAllFollowUps_caseworker_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/follow-ups/pending")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/followups/{id} — unknown id returns 404")
    void getFollowUp_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/follow-ups/99999")
                        .header("Authorization", caseworkerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PROVIDER POST /api/v1/followups — returns 403 (not 500)")
    void provider_postFollowUp_returns403() throws Exception {
        CreateFollowUpRequest req = CreateFollowUpRequest.builder()
                .referralId(1L)
                .type(FollowUpType.PROGRESS_REVIEW)
                .scheduledDate(LocalDateTime.now().plusDays(3))
                .build();

        mockMvc.perform(post("/api/v1/follow-ups")
                        .header("Authorization", providerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJson(req)))
                .andExpect(status().isForbidden());
    }
}
