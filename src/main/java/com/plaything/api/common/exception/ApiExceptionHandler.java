package com.plaything.api.common.exception;

import com.plaything.api.common.discord.DiscordAlarm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RequiredArgsConstructor
@RestControllerAdvice
public class ApiExceptionHandler {

    private final DiscordAlarm discordAlarm;

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<ErrorResponse> exceptionHandler(CustomException e, HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.toResponse(e);

        if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
            discordAlarm.sendServerErrorAlarm(e, request);
        }

        return ResponseEntity
                .status(errorResponse.httpStatus())
                .body(errorResponse);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleInternalServerError(Exception e, HttpServletRequest request, HttpServletResponse response) {
        discordAlarm.sendServerErrorAlarm(e, request);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
