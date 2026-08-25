package com.reentry.provider;

import com.reentry.provider.dto.CreateServiceProviderRequest;
import com.reentry.provider.dto.ServiceProviderResponse;
import com.reentry.provider.dto.UpdateServiceProviderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/providers")
@RequiredArgsConstructor
@Tag(name = "Service Providers", description = "Service provider management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ServiceProviderController {

    private final ServiceProviderService providerService;

    /**
     * Create a new service provider
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new service provider")
    public ResponseEntity<ServiceProviderResponse> createProvider(
            @Valid @RequestBody CreateServiceProviderRequest request) {
        ServiceProviderResponse response = providerService.createProvider(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get provider by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get service provider by ID")
    public ResponseEntity<ServiceProviderResponse> getProvider(@PathVariable Long id) {
        ServiceProviderResponse response = providerService.getProviderById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all providers
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get all active service providers")
    public ResponseEntity<List<ServiceProviderResponse>> getAllProviders() {
        List<ServiceProviderResponse> responses = providerService.getAllProviders();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get providers by city
     */
    @GetMapping("/city/{city}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get service providers by city")
    public ResponseEntity<List<ServiceProviderResponse>> getProvidersByCity(@PathVariable String city) {
        List<ServiceProviderResponse> responses = providerService.getProvidersByCity(city);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get verified providers
     */
    @GetMapping("/verified")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get verified service providers")
    public ResponseEntity<List<ServiceProviderResponse>> getVerifiedProviders() {
        List<ServiceProviderResponse> responses = providerService.getVerifiedProviders();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update provider
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update service provider")
    public ResponseEntity<ServiceProviderResponse> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody UpdateServiceProviderRequest request) {
        ServiceProviderResponse response = providerService.updateProvider(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify provider
     */
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify service provider")
    public ResponseEntity<ServiceProviderResponse> verifyProvider(@PathVariable Long id) {
        ServiceProviderResponse response = providerService.verifyProvider(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate provider
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate service provider")
    public ResponseEntity<Void> deactivateProvider(@PathVariable Long id) {
        providerService.deactivateProvider(id);
        return ResponseEntity.noContent().build();
    }
}
