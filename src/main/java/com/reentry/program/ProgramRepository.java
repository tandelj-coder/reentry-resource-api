package com.reentry.program;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByProviderId(Long providerId);
    List<Program> findByActiveTrue();
    List<Program> findByCity(String city);
    List<Program> findByCategory(ProgramCategory category);
    List<Program> findByIntakeStatus(IntakeStatus status);
    Optional<Program> findByIdAndActiveTrue(Long id);

    @Query("SELECT p FROM Program p WHERE p.active = true AND p.city = :city AND p.category = :category")
    List<Program> findByCityAndCategory(@Param("city") String city, @Param("category") ProgramCategory category);

    @Query("SELECT p FROM Program p WHERE p.active = true AND p.intakeStatus != 'CLOSED'")
    List<Program> findOpenPrograms();

    @Query("SELECT p FROM Program p WHERE p.active = true AND p.provider.id = :providerId")
    List<Program> findActiveByProviderId(@Param("providerId") Long providerId);
}
