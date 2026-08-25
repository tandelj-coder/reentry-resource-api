package com.reentry.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByAssignedCaseworkerId(Long caseworkerId);
    List<Client> findByActiveTrue();
    List<Client> findByCity(String city);
    Optional<Client> findByIdAndActiveTrue(Long id);

    @Query("SELECT c FROM Client c WHERE c.assignedCaseworker.id = :caseworkerId AND c.active = true")
    List<Client> findActiveByCaseworkerId(@Param("caseworkerId") Long caseworkerId);
}
