# Security Finding: @PreAuthorize AccessDeniedException Falls Through to 500

## Summary

During development of the Community Re-entry Support API, a defect was found where
role-based access-control denials returned **500 Internal Server Error** instead of
**403 Forbidden**. This allowed clients to infer that an endpoint exists and is
reachable, while masking the true denial reason.

---

## Root Cause

Spring Security's `@PreAuthorize` annotation throws an `AccessDeniedException` when
the authenticated user lacks the required role. The global exception handler in
`GlobalExceptionHandler.java` caught this via its catch-all `Exception` handler:

```java
// BEFORE fix — catch-all swallowed AccessDeniedException → 500
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, ...) {
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
}
```

`AccessDeniedException` is not a subclass of any of the explicitly handled
exception types (`ResourceNotFoundException`, `BadRequestException`, etc.),
so it fell through to the generic handler and was mapped to 500.

---

## Fix Applied

An explicit handler for `AccessDeniedException` was added **before** the catch-all,
returning the correct 403 status:

```java
// AFTER fix — explicit 403 for @PreAuthorize denials
@ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
public ResponseEntity<ErrorResponse> handleAccessDenied(
        AccessDeniedException ex,
        WebRequest request) {
    ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.FORBIDDEN.value())
            .error("Forbidden")
            .message("You do not have permission to access this resource")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
}
```

Alternatively, Spring Security's own `exceptionHandling()` DSL can be configured
in `SecurityConfig` to handle `AccessDeniedException` before it reaches the MVC
exception handler at all:

```java
http.exceptionHandling(ex -> ex
    .accessDeniedHandler((req, res, denied) -> {
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"Forbidden\",\"status\":403}");
    })
);
```

Both approaches produce the same observable result: a 403 response body consistent
with the API's standard `ErrorResponse` format.

---

## Impact

| Scenario | Before Fix | After Fix |
|---|---|---|
| PROVIDER calls `POST /api/v1/clients` | 500 Internal Server Error | **403 Forbidden** |
| CASEWORKER calls `GET /api/v1/audit-logs/*` | 500 Internal Server Error | **403 Forbidden** |
| PROVIDER calls `POST /api/v1/referrals` | 500 Internal Server Error | **403 Forbidden** |
| Any role calls admin-only endpoint | 500 Internal Server Error | **403 Forbidden** |

---

## Verification

49 automated tests in `src/test/` validate access rules across all roles and endpoints.
The access-control regression suite lives in:

```
src/test/java/com/reentry/security/AccessControlTest.java
```

Key assertions in that class:

```java
// Every @PreAuthorize denial must be 403, not 500
mockMvc.perform(post("/api/v1/clients")
        .header("Authorization", providerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJson(validClientRequest())))
    .andExpect(status().isForbidden());   // 403, never 500
```

---

## Lessons Learned

1. When using `@PreAuthorize` with a custom `GlobalExceptionHandler`, always add an
   explicit handler for `AccessDeniedException` before the generic `Exception` handler.
2. Test unauthorized access paths with the same rigour as authorized paths — a 500
   response is functionally a denial but provides incorrect status semantics.
3. Consider configuring Spring Security's `accessDeniedHandler` in `SecurityConfig`
   as a belt-and-suspenders measure, so the error format is consistent even if a
   future `@ExceptionHandler` ordering change reintroduces the bug.

---

*Identified and resolved by: Jigar Tandel  
Project: Community Re-entry Support API  
Date: September 2026*
