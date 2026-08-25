package com.reentry.client;

import com.reentry.client.dto.ClientResponse;
import com.reentry.client.dto.CreateClientRequest;
import com.reentry.client.dto.UpdateClientRequest;
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
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Client management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ClientController {

    private final ClientService clientService;

    /**
     * Create a new client
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Create a new client")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) {
        ClientResponse response = clientService.createClient(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get client by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN', 'PROVIDER')")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id) {
        ClientResponse response = clientService.getClientById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all clients
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Get all clients (filtered by role)")
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> responses = clientService.getAllClients();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update client
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Update client")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClientRequest request) {
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate client
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CASEWORKER', 'ADMIN')")
    @Operation(summary = "Deactivate client")
    public ResponseEntity<Void> deactivateClient(@PathVariable Long id) {
        clientService.deactivateClient(id);
        return ResponseEntity.noContent().build();
    }
}
