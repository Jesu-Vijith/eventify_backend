package com.eventmanagement.EventManagement.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class CustomException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private  String message;
    private  String errorMessage;
    private HttpStatus httpStatus;
    private  Exception exception;

    public CustomException(String message) {
        super(message);this.message = message;
    }


    public CustomException(String message, String errorMessage, HttpStatus httpStatus) {
        this.message = message;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    public CustomException(final String message, final HttpStatus httpStatus) {
        super();
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public CustomException( final String message, final HttpStatus httpStatus,Throwable ex) {
        super(ex);
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public CustomException(String message, String errorMessage, HttpStatus httpStatus, Exception exception) {
        this.message = message;
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
        this.exception = exception;
    }
}
