package com.reentry.provider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProvider, Long> {
    List<ServiceProvider> findByActiveTrue();
    List<ServiceProvider> findByCity(String city);
    List<ServiceProvider> findByVerifiedTrue();
    Optional<ServiceProvider> findByIdAndActiveTrue(Long id);
}
