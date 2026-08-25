package com.reentry.referral.dto;

import com.reentry.referral.ReferralStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ReferralResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long programId;
    private String programName;
    private Long caseworkerId;
    private String caseworkerName;
    private ReferralStatus status;
    private String notes;
    private LocalDateTime referredAt;
    private LocalDateTime completedAt;
    private String declinedReason;
    private LocalDateTime declineDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
