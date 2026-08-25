package com.reentry.program;

import com.reentry.program.dto.CreateProgramRequest;
import com.reentry.program.dto.ProgramResponse;
import com.reentry.program.dto.SearchProgramRequest;
import com.reentry.program.dto.UpdateProgramRequest;
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
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
@Tag(name = "Programs", description = "Program management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ProgramController {

    private final ProgramService programService;

    /**
     * Create a new program
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new program")
    public ResponseEntity<ProgramResponse> createProgram(
            @Valid @RequestBody CreateProgramRequest request) {
        ProgramResponse response = programService.createProgram(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get program by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get program by ID")
    public ResponseEntity<ProgramResponse> getProgram(@PathVariable Long id) {
        ProgramResponse response = programService.getProgramById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all programs
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get all active programs")
    public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
        List<ProgramResponse> responses = programService.getAllPrograms();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get programs by provider
     */
    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get programs by provider ID")
    public ResponseEntity<List<ProgramResponse>> getProgramsByProvider(
            @PathVariable Long providerId) {
        List<ProgramResponse> responses = programService.getProgramsByProvider(providerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get programs by city
     */
    @GetMapping("/city/{city}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get programs by city")
    public ResponseEntity<List<ProgramResponse>> getProgramsByCity(@PathVariable String city) {
        List<ProgramResponse> responses = programService.getProgramsByCity(city);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get programs by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get programs by category")
    public ResponseEntity<List<ProgramResponse>> getProgramsByCategory(
            @PathVariable String category) {
        ProgramCategory programCategory = ProgramCategory.valueOf(category.toUpperCase());
        List<ProgramResponse> responses = programService.getProgramsByCategory(programCategory);
        return ResponseEntity.ok(responses);
    }

    /**
     * Search programs by criteria
     */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Search programs by criteria")
    public ResponseEntity<List<ProgramResponse>> searchPrograms(
            @RequestBody SearchProgramRequest request) {
        List<ProgramResponse> responses = programService.searchPrograms(request);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get open programs
     */
    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASEWORKER', 'PROVIDER')")
    @Operation(summary = "Get open programs (not closed)")
    public ResponseEntity<List<ProgramResponse>> getOpenPrograms() {
        List<ProgramResponse> responses = programService.getOpenPrograms();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update program
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update program")
    public ResponseEntity<ProgramResponse> updateProgram(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProgramRequest request) {
        ProgramResponse response = programService.updateProgram(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate program
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate program")
    public ResponseEntity<Void> deactivateProgram(@PathVariable Long id) {
        programService.deactivateProgram(id);
        return ResponseEntity.noContent().build();
    }
}
