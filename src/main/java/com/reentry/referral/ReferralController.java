package com.reentry.referral;

import com.reentry.referral.dto.CreateReferralRequest;
import com.reentry.referral.dto.ReferralResponse;
import com.reentry.referral.dto.UpdateReferralStatusRequest;
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
@RequestMapping("/api/v1/referrals")
@RequiredArgsConstructor
@Tag(name = "Referrals", description = "Referral management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ReferralController {

    private final ReferralService referralService;

    /**
     * Create a new referral
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Create a new referral")
    public ResponseEntity<ReferralResponse> createReferral(
            @Valid @RequestBody CreateReferralRequest request) {
        ReferralResponse response = referralService.createReferral(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get referral by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN', 'PROVIDER')")
    @Operation(summary = "Get referral by ID")
    public ResponseEntity<ReferralResponse> getReferral(@PathVariable Long id) {
        ReferralResponse response = referralService.getReferralById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get referrals by client
     */
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Get referrals by client ID")
    public ResponseEntity<List<ReferralResponse>> getReferralsByClient(@PathVariable Long clientId) {
        List<ReferralResponse> responses = referralService.getReferralsByClient(clientId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get pending referrals
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Get pending referrals")
    public ResponseEntity<List<ReferralResponse>> getPendingReferrals() {
        List<ReferralResponse> responses = referralService.getPendingReferrals();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get referrals by program
     */
    @GetMapping("/program/{programId}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN', 'PROVIDER')")
    @Operation(summary = "Get referrals by program ID")
    public ResponseEntity<List<ReferralResponse>> getReferralsByProgram(@PathVariable Long programId) {
        List<ReferralResponse> responses = referralService.getReferralsByProgram(programId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Update referral status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Update referral status")
    public ResponseEntity<ReferralResponse> updateReferralStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReferralStatusRequest request) {
        ReferralResponse response = referralService.updateReferralStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
