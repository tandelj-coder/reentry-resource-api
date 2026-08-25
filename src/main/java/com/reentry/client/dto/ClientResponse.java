package com.reentry.client.dto;

import com.reentry.client.ClientNeed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ClientResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private Integer birthYear;
    private String city;
    private String postalCodePrefix;
    private Long assignedCaseworkerId;
    private String assignedCaseworkerName;
    private Set<ClientNeed> needs;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
