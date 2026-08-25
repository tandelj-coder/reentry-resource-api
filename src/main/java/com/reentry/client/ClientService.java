package com.reentry.client;

import com.reentry.client.dto.ClientResponse;
import com.reentry.client.dto.CreateClientRequest;
import com.reentry.client.dto.UpdateClientRequest;
import com.reentry.common.exception.ForbiddenException;
import com.reentry.common.exception.ResourceNotFoundException;
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
public class ClientService {

    private final ClientRepository clientRepository;
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
     * Create a new client (CASEWORKER only)
     */
    public ClientResponse createClient(CreateClientRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CASEWORKER && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only caseworkers and admins can create clients");
        }

        Client client = Client.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .birthYear(request.getBirthYear())
                .city(request.getCity())
                .postalCodePrefix(request.getPostalCodePrefix())
                .assignedCaseworker(currentUser)
                .needs(request.getNeeds() != null ? request.getNeeds() : java.util.HashSet.newHashSet(0))
                .active(true)
                .build();

        Client savedClient = clientRepository.save(client);
        log.info("Client created: {} by {}", savedClient.getId(), currentUser.getEmail());

        return mapToResponse(savedClient);
    }

    /**
     * Get client by ID
     */
    public ClientResponse getClientById(Long id) {
        User currentUser = getCurrentUser();
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        // Check authorization - Caseworkers can only see their own clients
        if (currentUser.getRole() == Role.CASEWORKER &&
                !client.getAssignedCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own clients");
        }

        return mapToResponse(client);
    }

    /**
     * Get all clients (filtered by role)
     */
    public List<ClientResponse> getAllClients() {
        User currentUser = getCurrentUser();

        List<Client> clients;
        if (currentUser.getRole() == Role.CASEWORKER) {
            clients = clientRepository.findActiveByCaseworkerId(currentUser.getId());
        } else if (currentUser.getRole() == Role.ADMIN) {
            clients = clientRepository.findByActiveTrue();
        } else {
            throw new ForbiddenException("Providers cannot view clients");
        }

        return clients.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Update client
     */
    public ClientResponse updateClient(Long id, UpdateClientRequest request) {
        User currentUser = getCurrentUser();
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        // Check authorization
        if (currentUser.getRole() == Role.CASEWORKER &&
                !client.getAssignedCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own clients");
        }

        if (request.getFirstName() != null) {
            client.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            client.setLastName(request.getLastName());
        }
        if (request.getBirthYear() != null) {
            client.setBirthYear(request.getBirthYear());
        }
        if (request.getCity() != null) {
            client.setCity(request.getCity());
        }
        if (request.getPostalCodePrefix() != null) {
            client.setPostalCodePrefix(request.getPostalCodePrefix());
        }
        if (request.getNeeds() != null) {
            client.setNeeds(request.getNeeds());
        }

        Client updatedClient = clientRepository.save(client);
        log.info("Client updated: {} by {}", id, currentUser.getEmail());

        return mapToResponse(updatedClient);
    }

    /**
     * Deactivate client
     */
    public void deactivateClient(Long id) {
        User currentUser = getCurrentUser();
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        if (currentUser.getRole() == Role.CASEWORKER &&
                !client.getAssignedCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only deactivate your own clients");
        }

        client.setActive(false);
        clientRepository.save(client);
        log.info("Client deactivated: {} by {}", id, currentUser.getEmail());
    }

    /**
     * Map Client entity to ClientResponse DTO
     */
    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .fullName(client.getFullName())
                .birthYear(client.getBirthYear())
                .city(client.getCity())
                .postalCodePrefix(client.getPostalCodePrefix())
                .assignedCaseworkerId(client.getAssignedCaseworker() != null ? client.getAssignedCaseworker().getId() : null)
                .assignedCaseworkerName(client.getAssignedCaseworker() != null ? client.getAssignedCaseworker().getFullName() : null)
                .needs(client.getNeeds())
                .active(client.getActive())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
