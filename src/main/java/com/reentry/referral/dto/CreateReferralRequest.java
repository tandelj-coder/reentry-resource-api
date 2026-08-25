package com.reentry.referral.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateReferralRequest {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Program ID is required")
    private Long programId;

    private String notes;
}
