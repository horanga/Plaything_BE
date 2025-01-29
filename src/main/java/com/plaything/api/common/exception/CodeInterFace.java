package com.plaything.api.common.exception;

import org.springframework.http.HttpStatus;

public interface CodeInterFace {

  String getMessage();

  HttpStatus getHttpStatus();
}
