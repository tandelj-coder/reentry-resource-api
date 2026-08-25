package com.reentry.followup;

import com.reentry.client.Client;
import com.reentry.client.ClientRepository;
import com.reentry.common.exception.ForbiddenException;
import com.reentry.common.exception.ResourceNotFoundException;
import com.reentry.followup.dto.CompleteFollowUpRequest;
import com.reentry.followup.dto.CreateFollowUpRequest;
import com.reentry.followup.dto.FollowUpResponse;
import com.reentry.referral.Referral;
import com.reentry.referral.ReferralRepository;
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
public class FollowUpService {

    private final FollowUpRepository followUpRepository;
    private final ReferralRepository referralRepository;
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
     * Create a new follow-up (CASEWORKER only)
     */
    public FollowUpResponse createFollowUp(CreateFollowUpRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.CASEWORKER && currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only caseworkers and admins can create follow-ups");
        }

        Referral referral = referralRepository.findById(request.getReferralId())
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found with id: " + request.getReferralId()));

        // Caseworkers can only create follow-ups for their own referrals
        if (currentUser.getRole() == Role.CASEWORKER &&
                !referral.getCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only create follow-ups for your own referrals");
        }

        Client client = referral.getClient();

        FollowUp followUp = FollowUp.builder()
                .referral(referral)
                .client(client)
                .caseworker(currentUser)
                .type(request.getType())
                .notes(request.getNotes())
                .scheduledDate(request.getScheduledDate())
                .completed(false)
                .build();

        FollowUp savedFollowUp = followUpRepository.save(followUp);
        log.info("Follow-up created: {} for referral {} by {}",
                savedFollowUp.getId(), referral.getId(), currentUser.getEmail());

        return mapToResponse(savedFollowUp);
    }

    /**
     * Get follow-up by ID
     */
    public FollowUpResponse getFollowUpById(Long id) {
        User currentUser = getCurrentUser();
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with id: " + id));

        // Check authorization
        if (currentUser.getRole() == Role.CASEWORKER &&
                !followUp.getCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view your own follow-ups");
        }

        return mapToResponse(followUp);
    }

    /**
     * Get all follow-ups for a referral
     */
    public List<FollowUpResponse> getFollowUpsByReferral(Long referralId) {
        User currentUser = getCurrentUser();
        Referral referral = referralRepository.findById(referralId)
                .orElseThrow(() -> new ResourceNotFoundException("Referral not found with id: " + referralId));

        // Check authorization
        if (currentUser.getRole() == Role.CASEWORKER &&
                !referral.getCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view follow-ups for your own referrals");
        }

        return followUpRepository.findByReferralId(referralId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all follow-ups for a client
     */
    public List<FollowUpResponse> getFollowUpsByClient(Long clientId) {
        User currentUser = getCurrentUser();
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));

        // Check authorization
        if (currentUser.getRole() == Role.CASEWORKER &&
                !client.getAssignedCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only view follow-ups for your own clients");
        }

        return followUpRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pending follow-ups for current caseworker
     */
    public List<FollowUpResponse> getPendingFollowUps() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.CASEWORKER) {
            return followUpRepository.findPendingByCaseworkerId(currentUser.getId())
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } else if (currentUser.getRole() == Role.ADMIN) {
            return followUpRepository.findByCompletedFalse()
                    .stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        throw new ForbiddenException("Only caseworkers and admins can view pending follow-ups");
    }

    /**
     * Get overdue follow-ups
     */
    public List<FollowUpResponse> getOverdueFollowUps() {
        return followUpRepository.findOverdueFollowUps(LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Complete a follow-up
     */
    public FollowUpResponse completeFollowUp(Long id, CompleteFollowUpRequest request) {
        User currentUser = getCurrentUser();
        FollowUp followUp = followUpRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Follow-up not found with id: " + id));

        // Check authorization
        if (currentUser.getRole() == Role.CASEWORKER &&
                !followUp.getCaseworker().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only complete your own follow-ups");
        }

        followUp.setCompleted(true);
        followUp.setCompletedDate(LocalDateTime.now());
        if (request.getNotes() != null) {
            followUp.setNotes(request.getNotes());
        }
        if (request.getClientFeedback() != null) {
            followUp.setClientFeedback(request.getClientFeedback());
        }
        if (request.getNextFollowUpDate() != null) {
            followUp.setNextFollowUpDate(request.getNextFollowUpDate());
        }

        FollowUp updatedFollowUp = followUpRepository.save(followUp);
        log.info("Follow-up {} completed by {}", id, currentUser.getEmail());

        return mapToResponse(updatedFollowUp);
    }

    /**
     * Map FollowUp entity to FollowUpResponse DTO
     */
    private FollowUpResponse mapToResponse(FollowUp followUp) {
        return FollowUpResponse.builder()
                .id(followUp.getId())
                .referralId(followUp.getReferral().getId())
                .clientId(followUp.getClient().getId())
                .clientName(followUp.getClient().getFullName())
                .caseworkerId(followUp.getCaseworker().getId())
                .caseworkerName(followUp.getCaseworker().getFullName())
                .type(followUp.getType())
                .notes(followUp.getNotes())
                .clientFeedback(followUp.getClientFeedback())
                .scheduledDate(followUp.getScheduledDate())
                .completedDate(followUp.getCompletedDate())
                .completed(followUp.getCompleted())
                .nextFollowUpDate(followUp.getNextFollowUpDate())
                .createdAt(followUp.getCreatedAt())
                .updatedAt(followUp.getUpdatedAt())
                .build();
    }
}
