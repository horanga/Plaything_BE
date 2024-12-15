package com.plaything.api.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<ErrorResponse> exceptionHandler(CustomException e, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.toResponse(e);

        return ResponseEntity
                .status(errorResponse.httpStatus())
                .body(errorResponse);
    }
}
