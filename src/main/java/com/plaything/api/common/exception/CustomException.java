package com.plaything.api.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private final CodeInterFace codeInterFace;

    public CustomException(CodeInterFace v){
        super(v.getMessage());
        this.codeInterFace= v;
    }

    public CustomException(CodeInterFace v, String message){
        super(v.getMessage()+message);
        this.codeInterFace= v;
    }
}


