package com.vesoft.nebula.graph.server.entity;

public enum ErrorCode {

    ERROR(true),

    SUCCESS(false);

    private final boolean errorCode;

    ErrorCode(boolean errorCode) {
        this.errorCode = errorCode;
    }

    public boolean getErrorCode() {
        return errorCode;
    }

}
