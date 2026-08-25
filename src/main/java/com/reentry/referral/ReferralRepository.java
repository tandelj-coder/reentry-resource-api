package com.reentry.referral;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByClientId(Long clientId);
    List<Referral> findByProgramId(Long programId);
    List<Referral> findByCaseworkerId(Long caseworkerId);
    List<Referral> findByStatus(ReferralStatus status);

    @Query("SELECT r FROM Referral r WHERE r.client.id = :clientId AND r.status = :status")
    List<Referral> findByClientIdAndStatus(@Param("clientId") Long clientId, @Param("status") ReferralStatus status);

    @Query("SELECT r FROM Referral r WHERE r.caseworker.id = :caseworkerId AND r.status = :status")
    List<Referral> findByCaseworkerIdAndStatus(@Param("caseworkerId") Long caseworkerId, @Param("status") ReferralStatus status);

    @Query("SELECT r FROM Referral r WHERE r.program.id = :programId AND r.status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS')")
    List<Referral> findActiveByProgramId(@Param("programId") Long programId);
}
