package com.reentry.followup.dto;

import com.reentry.followup.FollowUpType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class FollowUpResponse {
    private Long id;
    private Long referralId;
    private Long clientId;
    private String clientName;
    private Long caseworkerId;
    private String caseworkerName;
    private FollowUpType type;
    private String notes;
    private String clientFeedback;
    private LocalDateTime scheduledDate;
    private LocalDateTime completedDate;
    private Boolean completed;
    private LocalDateTime nextFollowUpDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
