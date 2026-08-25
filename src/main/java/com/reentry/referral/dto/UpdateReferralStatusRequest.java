package com.reentry.referral.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UpdateReferralStatusRequest {
    private String newStatus;
    private String notes;
    private String declinedReason;
}
