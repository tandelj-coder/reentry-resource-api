package com.reentry.program.dto;

import com.reentry.program.IntakeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class SearchProgramRequest {
    private String city;
    private String category;
    private IntakeStatus intakeStatus;
    private String keyword;
}
