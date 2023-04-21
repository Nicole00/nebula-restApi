package com.vesoft.nebula.graph.server.entity;

import java.io.Serializable;

public class NebulaConnectResponse implements Serializable {

    private int code;
    private String message;
    private String data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setResp(ErrorCode errorCode){
        this.code = errorCode.getErrorCode();
        this.message = errorCode.getErrorMsg();
    }

    @Override
    public String toString() {
        return "NebulaConnectResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
