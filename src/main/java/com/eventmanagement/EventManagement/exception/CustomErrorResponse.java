package com.eventmanagement.EventManagement.exception;

import lombok.Data;

import java.util.Date;

@Data
public class CustomErrorResponse {
    private String message;
    private String errorMessage;
    private int statusCode;
    private Date timestamp;


    public CustomErrorResponse(String message, String errorMessage, int statusCode) {
        this.message = message;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
        this.timestamp = new Date();
    }

    public CustomErrorResponse(String message, String errorMessage) {
        this.message = message;
        this.errorMessage = errorMessage;
    }
}
