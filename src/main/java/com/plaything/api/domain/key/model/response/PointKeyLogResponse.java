package com.plaything.api.domain.key.model.response;


import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Key 로그")
public record PointKeyLogResponse(

        @Schema(description = "key 로그 list")
        List<PointKeyLog> pointKeyLogList) {

}