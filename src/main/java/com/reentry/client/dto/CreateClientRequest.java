package com.reentry.client.dto;

import com.reentry.client.ClientNeed;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CreateClientRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private Integer birthYear;
    private String city;
    private String postalCodePrefix;
    private Set<ClientNeed> needs;
}
