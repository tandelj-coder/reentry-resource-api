package com.reentry.program;

import com.reentry.common.BaseEntity;
import com.reentry.provider.ServiceProvider;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "provider_id", nullable = false)
    private ServiceProvider provider;

    @NotBlank(message = "Program name is required")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgramCategory category;

    @Column(columnDefinition = "TEXT")
    private String eligibilityCriteria;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @Column(precision = 10, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "available_slots")
    private Integer availableSlots;

    @Enumerated(EnumType.STRING)
    @Column(name = "intake_status")
    private IntakeStatus intakeStatus = IntakeStatus.OPEN;

    @Column(name = "minimum_age")
    private Integer minimumAge;

    @Column(name = "maximum_age")
    private Integer maximumAge;

    @Column(nullable = false)
    private Boolean active = true;
}
