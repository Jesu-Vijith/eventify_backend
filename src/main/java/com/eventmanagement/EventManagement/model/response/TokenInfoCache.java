package com.eventmanagement.EventManagement.model.response;

import com.eventmanagement.EventManagement.model.entity.RoleEnum;
import com.eventmanagement.EventManagement.model.entity.Roles;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenInfoCache {
    private String userId;
    private String cognitoAccessToken;
    private String cognitoRefreshToken;
    private String idToken;
    private String refreshToken;

    private String name;
    private Long mobileNumber;
    private String aadharNumber;
    private String age;
    private String gender;
    private String profession;
    private String address;
    private String email;
    private Boolean isActive;
    private RoleEnum role;

}
