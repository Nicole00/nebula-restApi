/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.entity;

public class Result {
    private String out_nm;

    public Result(String out_nm){
        this.out_nm = out_nm;
    }

    public String getOut_nm() {
        return out_nm;
    }

    public void setOut_nm(String out_nm) {
        this.out_nm = out_nm;
    }

    @Override
    public String toString() {
        return "Result{" +
                "out_nm='" + out_nm + '\'' +
                '}';
    }
}
