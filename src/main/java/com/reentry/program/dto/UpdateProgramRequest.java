package com.reentry.program.dto;

import com.reentry.program.IntakeStatus;
import com.reentry.program.ProgramCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UpdateProgramRequest {
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
}
