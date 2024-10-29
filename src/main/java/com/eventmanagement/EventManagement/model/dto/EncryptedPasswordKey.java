package com.eventmanagement.EventManagement.model.dto;

import lombok.Data;

@Data
public class EncryptedPasswordKey {
    private String encryptedPassword;
    private String key;
}
