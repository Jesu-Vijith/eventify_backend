package com.eventmanagement.EventManagement.repository;

import com.eventmanagement.EventManagement.model.entity.RoleEnum;
import com.eventmanagement.EventManagement.model.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.cognitoidentityprovider.endpoints.internal.Value;

@Repository
public interface RolesRepository extends JpaRepository<Roles,String> {
    Roles findByRoleName(RoleEnum role);
}
