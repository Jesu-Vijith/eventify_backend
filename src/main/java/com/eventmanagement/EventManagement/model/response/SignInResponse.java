package com.eventmanagement.EventManagement.model.response;

import com.eventmanagement.EventManagement.model.entity.RoleEnum;
import com.eventmanagement.EventManagement.model.entity.Roles;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@Data
@NoArgsConstructor
public class SignInResponse {
    private String userId;
    private String idmAccessToken;
    private String idmRefreshToken;
    private Long expiresIn;
    private String email;
    private String name;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

}
