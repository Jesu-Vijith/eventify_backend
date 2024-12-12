
package com.eventmanagement.EventManagement.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    private String userId;

    // Common fields for both individual and company
    private String name;
    private Long mobileNumber;
    private String email;
    private Boolean isActive;

    @Transient
    private String password; // Transient to avoid persisting in DB

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    // Organizer Type (individual or company)
    @Enumerated(EnumType.STRING)
    private OrganizerType organizerType;

    // Fields for Individual Organizer
    private String aadharNumber;
    private String age;
    private String gender;
    private String profession;
    private String address;

    // Fields for Company Organizer
    private String companyName;
    private String contactPersonName;
    private String companyRegistrationNumber;
    private String phoneNumber;
    private String businessAddress;
    private String industryType;
    private String companyLogo;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "roleId", referencedColumnName = "roleId")
    private Roles roles;

//    // Many-to-many relation with Event, as one user can organize multiple events
//    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL)
//    private Set<Event> events;

    public enum OrganizerType {
        INDIVIDUAL,
        COMPANY
    }
}