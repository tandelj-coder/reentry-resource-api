package com.reentry.program.dto;

import com.reentry.program.ProgramCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateProgramRequest {
    @NotNull(message = "Provider ID is required")
    private Long providerId;

    @NotBlank(message = "Program name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private ProgramCategory category;

    private String eligibilityCriteria;

    @NotBlank(message = "City is required")
    private String city;

    private BigDecimal cost;
    private Integer capacity;
    private Integer availableSlots;
    private Integer minimumAge;
    private Integer maximumAge;
}
