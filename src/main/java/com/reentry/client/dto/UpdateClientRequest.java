package com.reentry.client.dto;

import com.reentry.client.ClientNeed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UpdateClientRequest {
    private String firstName;
    private String lastName;
    private Integer birthYear;
    private String city;
    private String postalCodePrefix;
    private Set<ClientNeed> needs;
}
