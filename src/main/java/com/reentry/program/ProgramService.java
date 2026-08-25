package com.reentry.program;

import com.reentry.common.exception.ForbiddenException;
import com.reentry.common.exception.ResourceNotFoundException;
import com.reentry.program.dto.CreateProgramRequest;
import com.reentry.program.dto.ProgramResponse;
import com.reentry.program.dto.SearchProgramRequest;
import com.reentry.program.dto.UpdateProgramRequest;
import com.reentry.provider.ServiceProvider;
import com.reentry.provider.ServiceProviderRepository;
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
public class ProgramService {

    private final ProgramRepository programRepository;
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
     * Create a new program (ADMIN only)
     */
    public ProgramResponse createProgram(CreateProgramRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can create programs");
        }

        ServiceProvider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Service provider not found with id: " + request.getProviderId()));

        Program program = Program.builder()
                .provider(provider)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .eligibilityCriteria(request.getEligibilityCriteria())
                .city(request.getCity())
                .cost(request.getCost())
                .capacity(request.getCapacity())
                .availableSlots(request.getAvailableSlots())
                .minimumAge(request.getMinimumAge())
                .maximumAge(request.getMaximumAge())
                .active(true)
                .build();

        Program savedProgram = programRepository.save(program);
        log.info("Program created: {} by {}", savedProgram.getId(), currentUser.getEmail());

        return mapToResponse(savedProgram);
    }

    /**
     * Get program by ID
     */
    public ProgramResponse getProgramById(Long id) {
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + id));
        return mapToResponse(program);
    }

    /**
     * Get all active programs
     */
    public List<ProgramResponse> getAllPrograms() {
        return programRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get programs by provider
     */
    public List<ProgramResponse> getProgramsByProvider(Long providerId) {
        return programRepository.findActiveByProviderId(providerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get programs by city
     */
    public List<ProgramResponse> getProgramsByCity(String city) {
        return programRepository.findByCity(city)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get programs by category
     */
    public List<ProgramResponse> getProgramsByCategory(ProgramCategory category) {
        return programRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search programs by criteria
     */
    public List<ProgramResponse> searchPrograms(SearchProgramRequest request) {
        List<Program> programs;

        if (request.getCity() != null && request.getCategory() != null) {
            ProgramCategory category = ProgramCategory.valueOf(request.getCategory().toUpperCase());
            programs = programRepository.findByCityAndCategory(request.getCity(), category);
        } else if (request.getCity() != null) {
            programs = programRepository.findByCity(request.getCity());
        } else if (request.getCategory() != null) {
            ProgramCategory category = ProgramCategory.valueOf(request.getCategory().toUpperCase());
            programs = programRepository.findByCategory(category);
        } else if (request.getIntakeStatus() != null) {
            programs = programRepository.findByIntakeStatus(request.getIntakeStatus());
        } else {
            programs = programRepository.findByActiveTrue();
        }

        // Apply keyword filter if provided
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            String keyword = request.getKeyword().toLowerCase();
            programs = programs.stream()
                    .filter(p -> p.getName().toLowerCase().contains(keyword) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());
        }

        return programs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get open programs (not closed)
     */
    public List<ProgramResponse> getOpenPrograms() {
        return programRepository.findOpenPrograms()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update program (ADMIN only)
     */
    public ProgramResponse updateProgram(Long id, UpdateProgramRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can update programs");
        }

        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + id));

        if (request.getName() != null) {
            program.setName(request.getName());
        }
        if (request.getDescription() != null) {
            program.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            program.setCategory(request.getCategory());
        }
        if (request.getEligibilityCriteria() != null) {
            program.setEligibilityCriteria(request.getEligibilityCriteria());
        }
        if (request.getCity() != null) {
            program.setCity(request.getCity());
        }
        if (request.getCost() != null) {
            program.setCost(request.getCost());
        }
        if (request.getCapacity() != null) {
            program.setCapacity(request.getCapacity());
        }
        if (request.getAvailableSlots() != null) {
            program.setAvailableSlots(request.getAvailableSlots());
        }
        if (request.getIntakeStatus() != null) {
            program.setIntakeStatus(request.getIntakeStatus());
        }
        if (request.getMinimumAge() != null) {
            program.setMinimumAge(request.getMinimumAge());
        }
        if (request.getMaximumAge() != null) {
            program.setMaximumAge(request.getMaximumAge());
        }

        Program updatedProgram = programRepository.save(program);
        log.info("Program updated: {} by {}", id, currentUser.getEmail());

        return mapToResponse(updatedProgram);
    }

    /**
     * Deactivate program (ADMIN only)
     */
    public void deactivateProgram(Long id) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can deactivate programs");
        }

        Program program = programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + id));

        program.setActive(false);
        programRepository.save(program);
        log.info("Program deactivated: {} by {}", id, currentUser.getEmail());
    }

    /**
     * Map Program entity to ProgramResponse DTO
     */
    private ProgramResponse mapToResponse(Program program) {
        return ProgramResponse.builder()
                .id(program.getId())
                .providerId(program.getProvider().getId())
                .providerName(program.getProvider().getOrganizationName())
                .name(program.getName())
                .description(program.getDescription())
                .category(program.getCategory())
                .eligibilityCriteria(program.getEligibilityCriteria())
                .city(program.getCity())
                .cost(program.getCost())
                .capacity(program.getCapacity())
                .availableSlots(program.getAvailableSlots())
                .intakeStatus(program.getIntakeStatus())
                .minimumAge(program.getMinimumAge())
                .maximumAge(program.getMaximumAge())
                .active(program.getActive())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .build();
    }
}
