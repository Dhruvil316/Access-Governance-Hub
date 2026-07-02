package com.dhruvil.auth_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {

    private LocalDateTime timeStamp;

    private String message;

    private T data;

    public ApiResponse() {
        this.timeStamp = LocalDateTime.now();
    }

    public ApiResponse(String message) {
        this();
        this.message = message;
    }

    public ApiResponse(String message, T data) {
        this();
        this.message = message;
        this.data = data;
    }
}