package com.plaything.api.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<ErrorResponse> exceptionHandler(CustomException e, HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // 또는 적절한 HTTP 상태코드
                .body(ErrorResponse.toResponse(e));
    }

}
