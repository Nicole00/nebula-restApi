package com.vesoft.nebula.graph.server.entity;

import com.vesoft.nebula.client.graph.data.ResultSet;
import java.io.Serializable;

public class NebulaQueryResponse implements Serializable {

    private Version version = new Version();

    /**
     * error code
     */
    private boolean error;

    /**
     * error message
     */
    private String message = null;

    private Result result;


    public NebulaQueryResponse(Result result) {
        error = ErrorCode.SUCCESS.getErrorCode();
        this.result = result;
    }

    public NebulaQueryResponse(boolean error, String message, Result result) {
        this.error = error;
        this.message = message;
        this.result = result;
    }


    public void setResp(ErrorCode errorCode) {
        error = errorCode.getErrorCode();
    }

    public boolean getError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public boolean isError() {
        return error;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
