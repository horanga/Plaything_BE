package com.plaything.api.common.exception;

import com.plaything.api.common.discord.DiscordAlarm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ApiExceptionHandler {

  private final DiscordAlarm discordAlarm;

  @ExceptionHandler({CustomException.class})
  public ResponseEntity<ErrorResponse> exceptionHandler(CustomException e,
      HttpServletRequest request) {
    ErrorResponse errorResponse = ErrorResponse.toResponse(e);

    if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
      discordAlarm.sendServerErrorAlarm(e, request);
    }

    return ResponseEntity
        .status(errorResponse.status())
        .body(errorResponse);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleInternalServerError(Exception e,
      HttpServletRequest request) {
    discordAlarm.sendServerErrorAlarm(e, request);

    ErrorResponse errorResponse = new ErrorResponse(
        "Internal Server Error",
        HttpStatus.INTERNAL_SERVER_ERROR.value());

    log.info(e.getMessage());

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(errorResponse);
  }
}
