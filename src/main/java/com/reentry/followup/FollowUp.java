package com.reentry.followup;

import com.reentry.client.Client;
import com.reentry.common.BaseEntity;
import com.reentry.referral.Referral;
import com.reentry.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "follow_ups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowUp extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "referral_id", nullable = false)
    private Referral referral;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "caseworker_id", nullable = false)
    private User caseworker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowUpType type;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String clientFeedback;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "next_follow_up_date")
    private LocalDateTime nextFollowUpDate;
}
