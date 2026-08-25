package com.reentry.provider;

import com.reentry.common.exception.ForbiddenException;
import com.reentry.common.exception.ResourceNotFoundException;
import com.reentry.provider.dto.CreateServiceProviderRequest;
import com.reentry.provider.dto.ServiceProviderResponse;
import com.reentry.provider.dto.UpdateServiceProviderRequest;
import com.reentry.user.Role;
import com.reentry.user.User;
import com.reentry.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ServiceProviderService {

    private final ServiceProviderRepository providerRepository;
    private final UserRepository userRepository;

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ForbiddenException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();
        String email = ((UserDetails) principal).getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Create a new service provider (ADMIN only)
     */
    public ServiceProviderResponse createProvider(CreateServiceProviderRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can create service providers");
        }

        ServiceProvider provider = ServiceProvider.builder()
                .organizationName(request.getOrganizationName())
                .description(request.getDescription())
                .phone(request.getPhone())
                .email(request.getEmail())
                .streetAddress(request.getStreetAddress())
                .city(request.getCity())
                .province(request.getProvince())
                .postalCode(request.getPostalCode())
                .verified(false)
                .active(true)
                .build();

        ServiceProvider savedProvider = providerRepository.save(provider);
        log.info("Service provider created: {} by {}", savedProvider.getId(), currentUser.getEmail());

        return mapToResponse(savedProvider);
    }

    /**
     * Get provider by ID
     */
    public ServiceProviderResponse getProviderById(Long id) {
        ServiceProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider not found with id: " + id));
        return mapToResponse(provider);
    }

    /**
     * Get all active providers
     */
    public List<ServiceProviderResponse> getAllProviders() {
        return providerRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get providers by city
     */
    public List<ServiceProviderResponse> getProvidersByCity(String city) {
        return providerRepository.findByCity(city)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get verified providers
     */
    public List<ServiceProviderResponse> getVerifiedProviders() {
        return providerRepository.findByVerifiedTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update provider (ADMIN only)
     */
    public ServiceProviderResponse updateProvider(Long id, UpdateServiceProviderRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can update service providers");
        }

        ServiceProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider not found with id: " + id));

        if (request.getOrganizationName() != null) {
            provider.setOrganizationName(request.getOrganizationName());
        }
        if (request.getDescription() != null) {
            provider.setDescription(request.getDescription());
        }
        if (request.getPhone() != null) {
            provider.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            provider.setEmail(request.getEmail());
        }
        if (request.getStreetAddress() != null) {
            provider.setStreetAddress(request.getStreetAddress());
        }
        if (request.getCity() != null) {
            provider.setCity(request.getCity());
        }
        if (request.getProvince() != null) {
            provider.setProvince(request.getProvince());
        }
        if (request.getPostalCode() != null) {
            provider.setPostalCode(request.getPostalCode());
        }

        ServiceProvider updatedProvider = providerRepository.save(provider);
        log.info("Service provider updated: {} by {}", id, currentUser.getEmail());

        return mapToResponse(updatedProvider);
    }

    /**
     * Verify provider (ADMIN only)
     */
    public ServiceProviderResponse verifyProvider(Long id) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can verify service providers");
        }

        ServiceProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider not found with id: " + id));

        provider.setVerified(true);
        ServiceProvider updatedProvider = providerRepository.save(provider);
        log.info("Service provider verified: {} by {}", id, currentUser.getEmail());

        return mapToResponse(updatedProvider);
    }

    /**
     * Deactivate provider (ADMIN only)
     */
    public void deactivateProvider(Long id) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can deactivate service providers");
        }

        ServiceProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service provider not found with id: " + id));

        provider.setActive(false);
        providerRepository.save(provider);
        log.info("Service provider deactivated: {} by {}", id, currentUser.getEmail());
    }

    /**
     * Map ServiceProvider entity to ServiceProviderResponse DTO
     */
    private ServiceProviderResponse mapToResponse(ServiceProvider provider) {
        return ServiceProviderResponse.builder()
                .id(provider.getId())
                .organizationName(provider.getOrganizationName())
                .description(provider.getDescription())
                .phone(provider.getPhone())
                .email(provider.getEmail())
                .streetAddress(provider.getStreetAddress())
                .city(provider.getCity())
                .province(provider.getProvince())
                .postalCode(provider.getPostalCode())
                .verified(provider.getVerified())
                .active(provider.getActive())
                .createdAt(provider.getCreatedAt())
                .updatedAt(provider.getUpdatedAt())
                .build();
    }
}
