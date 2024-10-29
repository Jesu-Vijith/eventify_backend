package com.eventmanagement.EventManagement.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    private String name;
    private Long mobileNumber;
    private String aadharNumber;
    private String age;
    private String gender;
    private String profession;
    private String address;
    private String email;
    private Boolean isActive;

//    @OneToMany (for organizer)
//    private Event event;

//    @OneToMany (for attendee)
//    private Seat seat;

    @Transient
    private String password;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    @ManyToOne(fetch = FetchType.EAGER, cascade ={CascadeType.MERGE,CascadeType.PERSIST})
    @JoinColumn(name = "roleId", referencedColumnName = "roleId")
    private Roles roles;

}
