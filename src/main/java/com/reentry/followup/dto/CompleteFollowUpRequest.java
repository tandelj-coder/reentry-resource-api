package com.reentry.followup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CompleteFollowUpRequest {
    private String notes;
    private String clientFeedback;
    private LocalDateTime nextFollowUpDate;
}
