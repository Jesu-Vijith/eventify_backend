package com.eventmanagement.EventManagement.model.request;

import com.eventmanagement.EventManagement.model.entity.RoleEnum;
import com.eventmanagement.EventManagement.model.entity.User;
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


    private String companyName;
    private String contactPersonName;
    private String companyRegistrationNumber;
    private String businessEmail;
    private String phoneNumber;
    private String businessAddress;
    private String industryType;
    private String companyLogo;
    private String organizerType;
}
