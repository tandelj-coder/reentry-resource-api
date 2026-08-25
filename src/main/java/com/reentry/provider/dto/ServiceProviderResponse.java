package com.reentry.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ServiceProviderResponse {
    private Long id;
    private String organizationName;
    private String description;
    private String phone;
    private String email;
    private String streetAddress;
    private String city;
    private String province;
    private String postalCode;
    private Boolean verified;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
