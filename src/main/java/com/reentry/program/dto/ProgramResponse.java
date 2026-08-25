package com.reentry.program.dto;

import com.reentry.program.IntakeStatus;
import com.reentry.program.ProgramCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ProgramResponse {
    private Long id;
    private Long providerId;
    private String providerName;
    private String name;
    private String description;
    private ProgramCategory category;
    private String eligibilityCriteria;
    private String city;
    private BigDecimal cost;
    private Integer capacity;
    private Integer availableSlots;
    private IntakeStatus intakeStatus;
    private Integer minimumAge;
    private Integer maximumAge;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
