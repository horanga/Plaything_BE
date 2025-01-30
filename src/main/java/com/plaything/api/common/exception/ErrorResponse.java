package com.plaything.api.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(

    @Schema(description = "에러 사유")
    String message,
    @Schema(description = "에러 사유")
    int status

) {

  public static ErrorResponse toResponse(CustomException e) {
    return new ErrorResponse(e.getMessage(), e.getCodeInterFace().getHttpStatus().value());
  }
}
