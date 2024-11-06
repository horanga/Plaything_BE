package com.plaything.api.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode implements CodeInterFace {
    SUCCESS(0, "SUCCESS"),
    USER_ALREADY_EXISTS(-1, "USER_ALREADY_EXISTS"),
    USER_SAVED_FAILED(-2, "USER_SAVED_FAILED"),
    NOT_EXIST_USER(-3, "NOT_EXIST_USER"),
    MIS_MATCH_PASSWORD(-4, "MIS_MATCH_PASSWORD"),

    TOKEN_IS_INVALID(-200, "TOKEN_IS_INVALID"),
    ACCESS_TOKEN_IS_NOT_EXPIRED(-201, "TOKEN_IS_NOT_EXPIRED"),
    TOKEN_IS_EXPIRED(-202,"TOKEN_IS_EXPIRED"),

    EXTENSION_IS_INVALID(-400, "EXTENSION_IS_INVALID"),
    SIZE_IS_INVALID(-401, "SIZE_IS_INVALID"),
    CONTENT_TYPE_IS_INVALID(-402, "CONTENT_TYPE_IS_INVALID"),
    IMAGE_SAVED_FAILED(-403, "IMAGE_SAVED_FAILED"),
    EXCEED_IMAGE_LIMIT(-404, "EXCEED_IMAGE_LIMIT"),
    NO_IMAGE_FAILED(-404, "NO_IMAGE_FAILED"),

    NOT_EXIST_PROFILE(-501, "NOT_EXIST_PROFILE"),
    PROFILE_REGISTER_FAILED(-502, "PROFILE_REGISTER_FAILED"),
    PROFILE_ALREADY_EXIST(-503, "PROFILE ALREADY EXIST"),

    NOT_EXIST_PROFILE_RECORD(-601, "NOT EXIST PROFILE RECORD"),

    NOT_EXIST_PRIMARY_TRAIT(-701, "NOT EXIST PRIMARY TRAIT"),
    TRAITS_NOT_INCLUDE_PRIMARY(-702, "TRAITS NOT INCLUDE PRIMARY TRAIT"),

    MATCHING_FAIL_WITHOUT_IMAGE(-801, "MATCHING_FAIL_WITHOUT_IMAGE"),
    MATCHING_FAIL_WITH_BAN_PROFILE(-802, "MATCHING_FAIL_WITH_BAN_PROFILE");


    private final Integer code;
    private final String message;

}
