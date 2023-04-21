package com.vesoft.nebula.graph.server.entity;

import java.io.Serializable;

public class NebulaQueryRequest implements Serializable {
    private String statement;

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    @Override
    public String toString() {
        return "NebulaQueryRequest{" +
                "statement='" + statement + '\'' +
                '}';
    }
}
