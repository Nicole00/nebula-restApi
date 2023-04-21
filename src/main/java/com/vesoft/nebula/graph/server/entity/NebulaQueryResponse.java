package com.vesoft.nebula.graph.server.entity;

import com.vesoft.nebula.client.graph.data.ResultSet;
import java.io.Serializable;

public class NebulaQueryResponse implements Serializable {

    /** error code */
    private int code;

    /** error message */
    private String message;

    private int rowCount;

    private long latency;

    public NebulaQueryResponse(){
        code = ErrorCode.SUCCESS.getErrorCode();
        message = ErrorCode.SUCCESS.getErrorMsg();
    }

    public void setResp(ErrorCode errorCode){
        code = errorCode.getErrorCode();
        message = errorCode.getErrorMsg();
    }

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

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }
}
