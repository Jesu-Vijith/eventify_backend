package com.eventmanagement.EventManagement.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="roles")
public class Roles {
    @Id
    private String roleId;

    @Enumerated(EnumType.STRING)
    private RoleEnum roleName;

    private String description;
    private boolean isActive;


}
