package com.eventmanagement.EventManagement.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.crypto.IllegalBlockSizeException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLTransientConnectionException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(final WebRequest webRequest, final ErrorAttributeOptions includeStackTrace) {
                final Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, includeStackTrace);
                errorAttributes.remove("exception");
                return errorAttributes;
            }
        };
    }
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<CustomErrorResponse> handleCustomException(CustomException ex, WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ex.getMessage(),
                ex.getErrorMessage() != null ? ex.getErrorMessage() : null,
                ex.getHttpStatus().value()
        );
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ResponseMessage> handleAuthorizationException(final HttpServletResponse res, final MissingRequestHeaderException e) throws IOException {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("RequestHeader Missing"));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) ->{
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        return new ResponseEntity<Object>(errors, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseMessage> handleIllegalArgumentException(final HttpServletResponse res,final IllegalArgumentException e) throws IOException{
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("Argument is not correct one"));
    }

    @ExceptionHandler(NoSuchAlgorithmException.class)
    public ResponseEntity<ResponseMessage> handleNoSuchAlgorithmException(final HttpServletResponse res,final NoSuchAlgorithmException e) throws IOException{
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("No such algorithm to convert it"));
    }

    @ExceptionHandler(InvalidKeyException.class)
    public ResponseEntity<ResponseMessage> handleInvalidKeyException(final HttpServletResponse res,final InvalidKeyException e) throws IOException{
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("Invalid key supplied"));
    }


    @ExceptionHandler(IllegalBlockSizeException.class)
    public ResponseEntity<ResponseMessage> handleIllegalBlockSizeException(final HttpServletResponse res,final IllegalBlockSizeException e) throws IOException{
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("size is too large"));
    }


    @ExceptionHandler(InvalidKeySpecException.class)
    public ResponseEntity<ResponseMessage> handleInvalidKeySpecException(final HttpServletResponse res,final InvalidKeySpecException e) throws IOException{
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseMessage("Invalid key specification"));
    }

    @ExceptionHandler(NullPointerException.class)
    public  ResponseEntity<ResponseMessage> handleNullPointerException(final HttpServletResponse res,final NullPointerException e) throws IOException{
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Null Pointer Exception"));
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ResponseMessage> invalidDataAccessApiUsageException(final HttpServletResponse res,final InvalidDataAccessApiUsageException e){
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Repository operation cannot be processed with null value"));
    }

    @ExceptionHandler({ SQLTransientConnectionException.class, JDBCConnectionException.class})
    public ResponseEntity<CustomErrorResponse> handleGeneralException( WebRequest request) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                "Internal Server Error",
                "DB connection error",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<CustomErrorResponse> handleBindException(BindException ex, WebRequest request) {
        StringBuilder errorMessage = new StringBuilder();
        ex.getFieldErrors().forEach(error -> {
            errorMessage.append("Field '")
                    .append(error.getField())
                    .append("' - ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        });
        CustomErrorResponse customErrorResponse = new CustomErrorResponse(
                "Validation failed for request - check you request ",
                errorMessage.toString(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(customErrorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        ex.printStackTrace();
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ex.getMessage().equals("Access is denied") ? "Access is denied": "Internal server error",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
