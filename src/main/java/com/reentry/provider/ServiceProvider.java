package com.reentry.provider;

import com.reentry.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "service_providers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceProvider extends BaseEntity {
    @NotBlank(message = "Organization name is required")
    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "phone")
    private String phone;

    @Email(message = "Email should be valid")
    @Column(name = "email")
    private String email;

    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "province")
    private String province;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private Boolean active = true;
}
