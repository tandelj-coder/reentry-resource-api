package com.reentry.referral;

import com.reentry.client.Client;
import com.reentry.common.BaseEntity;
import com.reentry.program.Program;
import com.reentry.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "referrals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Referral extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne
    @JoinColumn(name = "caseworker_id", nullable = false)
    private User caseworker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReferralStatus status = ReferralStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "referred_at", nullable = false)
    private LocalDateTime referredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "declined_reason")
    private String declinedReason;

    @Column(name = "decline_date")
    private LocalDateTime declineDate;
}
