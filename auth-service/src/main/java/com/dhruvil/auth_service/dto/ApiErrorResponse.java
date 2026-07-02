package com.dhruvil.auth_service.dto;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ApiErrorResponse {
    private LocalDateTime timeStamp ;
    private String error ;
    private HttpStatus statusCode ;

    public ApiErrorResponse ( ) {
        this.timeStamp = LocalDateTime.now() ;
    }

    public ApiErrorResponse ( String error , HttpStatus statusCode ) {
        this();
        this.error = error ;
        this.statusCode = statusCode ;
    }
}
