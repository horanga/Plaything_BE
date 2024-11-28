package com.plaything.api.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record ErrorResponse (

    @Schema(description = "에러 사유")
    String message

) {

    public static ErrorResponse toResponse(CustomException e) {
        return new ErrorResponse(e.getMessage());
    }
}
