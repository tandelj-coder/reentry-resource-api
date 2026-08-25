package com.reentry.audit;

import com.reentry.audit.dto.AuditLogResponse;
import com.reentry.audit.dto.CreateAuditLogRequest;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Get current authenticated user
     */
    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return Optional.empty();
        }

        String email = ((UserDetails) principal).getUsername();
        return userRepository.findByEmail(email);
    }

    /**
     * Log an action
     */
    public AuditLogResponse logAction(CreateAuditLogRequest request) {
        Optional<User> currentUser = getCurrentUser();

        AuditLog auditLog = AuditLog.builder()
                .user(currentUser.orElse(null))
                .action(request.getAction())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .oldValue(request.getOldValue())
                .newValue(request.getNewValue())
                .ipAddress(request.getIpAddress())
                .description(request.getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        AuditLog savedLog = auditLogRepository.save(auditLog);
        log.debug("Action logged: {} on {} with id {}", request.getAction(), request.getEntityType(), request.getEntityId());

        return mapToResponse(savedLog);
    }

    /**
     * Get audit logs by user
     */
    public List<AuditLogResponse> getAuditLogsByUser(Long userId) {
        User currentUser = getCurrentUserRequired();

        // Only admins can view audit logs
        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can view audit logs");
        }

        return auditLogRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by action
     */
    public List<AuditLogResponse> getAuditLogsByAction(AuditAction action) {
        User currentUser = getCurrentUserRequired();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can view audit logs");
        }

        return auditLogRepository.findByAction(action)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by entity type
     */
    public List<AuditLogResponse> getAuditLogsByEntityType(String entityType) {
        User currentUser = getCurrentUserRequired();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can view audit logs");
        }

        return auditLogRepository.findByEntityType(entityType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by entity ID
     */
    public List<AuditLogResponse> getAuditLogsByEntityId(Long entityId) {
        User currentUser = getCurrentUserRequired();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can view audit logs");
        }

        return auditLogRepository.findByEntityId(entityId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs by date range
     */
    public List<AuditLogResponse> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = getCurrentUserRequired();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can view audit logs");
        }

        return auditLogRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get audit logs for entity type and ID with history
     */
    public List<AuditLogResponse> getAuditHistoryForEntity(String entityType, Long entityId) {
        User currentUser = getCurrentUserRequired();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can view audit logs");
        }

        return auditLogRepository.findByEntityTypeAndId(entityType, entityId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get current authenticated user (required)
     */
    private User getCurrentUserRequired() {
        return getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));
    }

    /**
     * Map AuditLog entity to AuditLogResponse DTO
     */
    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .userName(auditLog.getUser() != null ? auditLog.getUser().getFullName() : "SYSTEM")
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .ipAddress(auditLog.getIpAddress())
                .description(auditLog.getDescription())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
