package com.reentry.referral;

import com.reentry.client.Client;
import com.reentry.client.ClientRepository;
import com.reentry.common.exception.ForbiddenException;
import com.reentry.common.exception.ResourceNotFoundException;
import com.reentry.program.Program;
import com.reentry.program.ProgramRepository;
import com.reentry.referral.dto.CreateReferralRequest;
import com.reentry.referral.dto.ReferralResponse;
import com.reentry.referral.dto.UpdateReferralStatusRequest;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final ClientRepository clientRepository;
    private final ProgramRepository programRepository;
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
     * Create a new referral (CASEWORKER only)
     */
    public ReferralResponse createReferral(CreateReferralRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CASEWORKER && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only caseworkers and admins can create referrals");
        }

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + request.getClientId()));

        // Caseworkers can only refer their own clients
        if (currentUser.getRole() == Role.CASEWORKER &&
                !client.getAssignedCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only refer your own clients");
        }

        Program program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + request.getProgramId()));

        Referral referral = Referral.builder()
                .client(client)
                .program(program)
                .caseworker(currentUser)
                .status(ReferralStatus.PENDING)
                .notes(request.getNotes())
                .referredAt(LocalDateTime.now())
                .build();

        Referral savedReferral = referralRepository.save(referral);
        log.info("Referral created: {} for client {} to program {} by {}",
                savedReferral.getId(), client.getId(), program.getId(), currentUser.getEmail());

        return mapToResponse(savedReferral);
    }

    /**
     * Get referral by ID
     */
    public ReferralResponse getReferralById(Long id) {
        User currentUser = getCurrentUser();
        Referral referral = referralRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found with id: " + id));

        // Check authorization - Caseworkers can only see their own referrals
        if (currentUser.getRole() == Role.CASEWORKER &&
                !referral.getCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own referrals");
        }

        return mapToResponse(referral);
    }

    /**
     * Get all referrals for a client
     */
    public List<ReferralResponse> getReferralsByClient(Long clientId) {
        User currentUser = getCurrentUser();
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        // Check authorization
        if (currentUser.getRole() == Role.CASEWORKER &&
                !client.getAssignedCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view referrals for your own clients");
        }

        return referralRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending referrals for a caseworker
     */
    public List<ReferralResponse> getPendingReferrals() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.CASEWORKER) {
            return referralRepository.findByCaseworkerIdAndStatus(currentUser.getId(), ReferralStatus.PENDING)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (currentUser.getRole() == Role.ADMIN) {
            return referralRepository.findByStatus(ReferralStatus.PENDING)
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        throw new ForbiddenException("Only caseworkers and admins can view pending referrals");
    }

    /**
     * Get referrals by program
     */
    public List<ReferralResponse> getReferralsByProgram(Long programId) {
        return referralRepository.findActiveByProgramId(programId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update referral status
     */
    public ReferralResponse updateReferralStatus(Long id, UpdateReferralStatusRequest request) {
        User currentUser = getCurrentUser();
        Referral referral = referralRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found with id: " + id));

        // Check authorization
        if (currentUser.getRole() == Role.CASEWORKER &&
                !referral.getCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only update your own referrals");
        }

        ReferralStatus newStatus = ReferralStatus.valueOf(request.getNewStatus().toUpperCase());

        // Validate status transitions
        if (!isValidStatusTransition(referral.getStatus(), newStatus)) {
            throw new ForbiddenException("Invalid status transition from " + referral.getStatus() + " to " + newStatus);
        }

        referral.setStatus(newStatus);
        if (request.getNotes() != null) {
            referral.setNotes(request.getNotes());
        }

        if (newStatus == ReferralStatus.COMPLETED) {
            referral.setCompletedAt(LocalDateTime.now());
        } else if (newStatus == ReferralStatus.DECLINED) {
            referral.setDeclineDate(LocalDateTime.now());
            referral.setDeclinedReason(request.getDeclinedReason());
        }

        Referral updatedReferral = referralRepository.save(referral);
        log.info("Referral {} status updated to {} by {}", id, newStatus, currentUser.getEmail());

        return mapToResponse(updatedReferral);
    }

    /**
     * Validate status transitions
     */
    private boolean isValidStatusTransition(ReferralStatus from, ReferralStatus to) {
        if (from == to) return true;

        return switch (from) {
            case PENDING -> to == ReferralStatus.ACCEPTED || to == ReferralStatus.DECLINED || to == ReferralStatus.CANCELLED;
            case ACCEPTED -> to == ReferralStatus.IN_PROGRESS || to == ReferralStatus.DECLINED || to == ReferralStatus.CANCELLED;
            case IN_PROGRESS -> to == ReferralStatus.COMPLETED || to == ReferralStatus.DECLINED || to == ReferralStatus.CANCELLED;
            case COMPLETED, DECLINED, CANCELLED -> false;
        };
    }

    /**
     * Map Referral entity to ReferralResponse DTO
     */
    private ReferralResponse mapToResponse(Referral referral) {
        return ReferralResponse.builder()
                .id(referral.getId())
                .clientId(referral.getClient().getId())
                .clientName(referral.getClient().getFullName())
                .programId(referral.getProgram().getId())
                .programName(referral.getProgram().getName())
                .caseworkerId(referral.getCaseworker().getId())
                .caseworkerName(referral.getCaseworker().getFullName())
                .status(referral.getStatus())
                .notes(referral.getNotes())
                .referredAt(referral.getReferredAt())
                .completedAt(referral.getCompletedAt())
                .declinedReason(referral.getDeclinedReason())
                .declineDate(referral.getDeclineDate())
                .createdAt(referral.getCreatedAt())
                .updatedAt(referral.getUpdatedAt())
                .build();
    }
}
