package com.plaything.api.domain.image.service.model;


import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Saved Image in S3")
public record SavedImage(
        @Schema(description = "S3 file url")
        String url,
        @Schema(description = "S3 fileName")
        String fileName
) {}