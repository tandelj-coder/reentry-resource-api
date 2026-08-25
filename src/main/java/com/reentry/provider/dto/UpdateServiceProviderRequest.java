package com.reentry.provider.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UpdateServiceProviderRequest {
    private String organizationName;
    private String description;
    private String phone;
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String streetAddress;
    private String city;
    private String province;
    private String postalCode;
}
