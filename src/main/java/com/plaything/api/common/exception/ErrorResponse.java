package com.plaything.api.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

public record ErrorResponse(

        @Schema(description = "에러 사유")
        String message,
        @Schema(description = "에러 사유")
        HttpStatus httpStatus

) {

    public static ErrorResponse toResponse(CustomException e) {
        return new ErrorResponse(e.getMessage(), e.getCodeInterFace().getHttpStatus());
    }
}
