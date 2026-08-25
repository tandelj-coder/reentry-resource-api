package com.reentry.client;

import com.reentry.common.BaseEntity;
import com.reentry.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client extends BaseEntity {
    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(name = "city")
    private String city;

    @Column(name = "postal_code_prefix")
    private String postalCodePrefix;

    @ManyToOne
    @JoinColumn(name = "assigned_caseworker_id")
    private User assignedCaseworker;

    @ElementCollection(targetClass = ClientNeed.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "client_needs", joinColumns = @JoinColumn(name = "client_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "need")
    private Set<ClientNeed> needs = new HashSet<>();

    @Column(nullable = false)
    private Boolean active = true;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
