package com.plaything.api.common.exception;

import org.springframework.http.HttpStatus;

public enum CustomHttpStatus {

  BAD_REQUEST(HttpStatus.BAD_REQUEST),
  NOT_FOUND(HttpStatus.NOT_FOUND),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
  CONFLICT(HttpStatus.CONFLICT),
  UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY),
  FORBIDDEN(HttpStatus.FORBIDDEN),
  TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);


  private final HttpStatus status;
  private final int code;
  private final String message;

  CustomHttpStatus(HttpStatus httpStatus) {
    this.status = httpStatus;
    this.code = httpStatus.value();
    this.message = httpStatus.getReasonPhrase();
  }
}
