package com.reentry.followup.dto;

import com.reentry.followup.FollowUpType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateFollowUpRequest {
    @NotNull(message = "Referral ID is required")
    private Long referralId;

    @NotNull(message = "Follow-up type is required")
    private FollowUpType type;

    @NotNull(message = "Scheduled date is required")
    private LocalDateTime scheduledDate;

    private String notes;
}
