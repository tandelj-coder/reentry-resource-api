package com.reentry.followup;

import com.reentry.followup.dto.CompleteFollowUpRequest;
import com.reentry.followup.dto.CreateFollowUpRequest;
import com.reentry.followup.dto.FollowUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/follow-ups")
@RequiredArgsConstructor
@Tag(name = "Follow-ups", description = "Follow-up management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class FollowUpController {

    private final FollowUpService followUpService;

    /**
     * Create a new follow-up
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Create a new follow-up")
    public ResponseEntity<FollowUpResponse> createFollowUp(
            @Valid @RequestBody CreateFollowUpRequest request) {
        FollowUpResponse response = followUpService.createFollowUp(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get follow-up by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Get follow-up by ID")
    public ResponseEntity<FollowUpResponse> getFollowUp(@PathVariable Long id) {
        FollowUpResponse response = followUpService.getFollowUpById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get follow-ups by referral
     */
    @GetMapping("/referral/{referralId}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Get follow-ups by referral ID")
    public ResponseEntity<List<FollowUpResponse>> getFollowUpsByReferral(
            @PathVariable Long referralId) {
        List<FollowUpResponse> responses = followUpService.getFollowUpsByReferral(referralId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get follow-ups by client
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Get follow-ups by client ID")
    public ResponseEntity<List<FollowUpResponse>> getFollowUpsByClient(
            @PathVariable Long clientId) {
        List<FollowUpResponse> responses = followUpService.getFollowUpsByClient(clientId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get pending follow-ups
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Get pending follow-ups")
    public ResponseEntity<List<FollowUpResponse>> getPendingFollowUps() {
        List<FollowUpResponse> responses = followUpService.getPendingFollowUps();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get overdue follow-ups
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Get overdue follow-ups")
    public ResponseEntity<List<FollowUpResponse>> getOverdueFollowUps() {
        List<FollowUpResponse> responses = followUpService.getOverdueFollowUps();
        return ResponseEntity.ok(responses);
    }

    /**
     * Complete a follow-up
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Complete a follow-up")
    public ResponseEntity<FollowUpResponse> completeFollowUp(
            @PathVariable Long id,
            @Valid @RequestBody CompleteFollowUpRequest request) {
        FollowUpResponse response = followUpService.completeFollowUp(id, request);
        return ResponseEntity.ok(response);
    }
}
