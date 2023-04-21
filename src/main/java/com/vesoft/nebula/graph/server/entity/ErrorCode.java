package com.vesoft.nebula.graph.server.entity;

public enum ErrorCode {

    ERROR(-1, "FAILED"),

    SUCCESS(0, "SUCCESS"),

    SESSION_POOL_INIT_ERROR(1, "session pool init failed."),

    CONNECT_ERROR(2, "broken pipe"),

    PARAM_ERROR(3, "param error");


    private final int errorCode;
    private final String errorMsg;

    ErrorCode(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }


    /**
     * get ErrorCode according to the code
     */
    public static ErrorCode getErrorCode(int code) {
        for(ErrorCode errorCode: ErrorCode.values()){
            if(code == errorCode.getErrorCode()){
                return errorCode;
            }
        }
        return ERROR;
    }
}
