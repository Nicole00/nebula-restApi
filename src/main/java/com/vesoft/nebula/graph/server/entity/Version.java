/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.entity;

public class Version {
    private String edition = "enterprise";
    private String api="v2";
    private int schema=141;

    public Version(){}

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public int getSchema() {
        return schema;
    }

    public void setSchema(int schema) {
        this.schema = schema;
    }

    @Override
    public String toString() {
        return "Version{" +
                "edition='" + edition + '\'' +
                ", api='" + api + '\'' +
                ", schema=" + schema +
                '}';
    }
}
