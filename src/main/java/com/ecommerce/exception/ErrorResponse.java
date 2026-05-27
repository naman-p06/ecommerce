package com.ecommerce.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;
    public ErrorResponse(int status, String error, String message, String path) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.path      = path;
        this.timestamp = LocalDateTime.now();
    }
}
