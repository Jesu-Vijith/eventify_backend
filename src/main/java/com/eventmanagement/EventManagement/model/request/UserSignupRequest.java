package com.eventmanagement.EventManagement.model.request;

import com.eventmanagement.EventManagement.model.entity.RoleEnum;
import lombok.Data;

@Data
public class UserSignupRequest {

    private String uniqueId;
    private String name;
    private Long mobileNumber;
    private String email;
    private String aadharNumber;
    private String age;
    private String gender;
    private String profession;
    private String address;
    private String password;

    private String userType;
}
