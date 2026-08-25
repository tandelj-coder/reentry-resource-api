package com.reentry.audit;

import com.reentry.audit.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit trail and logging endpoints")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Get audit logs by user
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs by user")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(@PathVariable Long userId) {
        List<AuditLogResponse> responses = auditLogService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get audit logs by action
     */
    @GetMapping("/action/{action}")
    @Operation(summary = "Get audit logs by action")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByAction(@PathVariable AuditAction action) {
        List<AuditLogResponse> responses = auditLogService.getAuditLogsByAction(action);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get audit logs by entity type
     */
    @GetMapping("/entity-type/{entityType}")
    @Operation(summary = "Get audit logs by entity type")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByEntityType(
            @PathVariable String entityType) {
        List<AuditLogResponse> responses = auditLogService.getAuditLogsByEntityType(entityType);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get audit logs by entity ID
     */
    @GetMapping("/entity/{entityId}")
    @Operation(summary = "Get audit logs by entity ID")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByEntityId(
            @PathVariable Long entityId) {
        List<AuditLogResponse> responses = auditLogService.getAuditLogsByEntityId(entityId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get audit logs by date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get audit logs by date range")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLogResponse> responses = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get audit history for entity
     */
    @GetMapping("/history/{entityType}/{entityId}")
    @Operation(summary = "Get audit history for a specific entity")
    public ResponseEntity<List<AuditLogResponse>> getAuditHistoryForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<AuditLogResponse> responses = auditLogService.getAuditHistoryForEntity(entityType, entityId);
        return ResponseEntity.ok(responses);
    }
}
