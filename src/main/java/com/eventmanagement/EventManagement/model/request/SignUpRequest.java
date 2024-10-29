package com.eventmanagement.EventManagement.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;



@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {
    @NotEmpty(message = "UserName is required")
    @Pattern(regexp = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$",
            message = "Invalid  mailId composition")
    public String email;

    @NotEmpty(message = "ConfirmationCode is required")
    public String confirmationCode;

    private String uniqueId;
    private String password;
}
