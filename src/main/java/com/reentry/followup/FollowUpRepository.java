package com.reentry.followup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
    List<FollowUp> findByClientId(Long clientId);
    List<FollowUp> findByReferralId(Long referralId);
    List<FollowUp> findByCaseworkerId(Long caseworkerId);
    List<FollowUp> findByCompletedFalse();
    List<FollowUp> findByType(FollowUpType type);

    @Query("SELECT f FROM FollowUp f WHERE f.caseworker.id = :caseworkerId AND f.completed = false")
    List<FollowUp> findPendingByCaseworkerId(@Param("caseworkerId") Long caseworkerId);

    @Query("SELECT f FROM FollowUp f WHERE f.completed = false AND f.scheduledDate <= :now")
    List<FollowUp> findOverdueFollowUps(@Param("now") LocalDateTime now);

    @Query("SELECT f FROM FollowUp f WHERE f.caseworker.id = :caseworkerId AND f.scheduledDate BETWEEN :startDate AND :endDate")
    List<FollowUp> findByDateRangeAndCaseworker(@Param("caseworkerId") Long caseworkerId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);
}
