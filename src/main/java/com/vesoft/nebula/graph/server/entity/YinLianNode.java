/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.entity;

public class YinLianNode {
    private String tagName;

    // entname for ins, or name_cn for people
    private String name;

    public YinLianNode(String tagName, String name){
        this.tagName = tagName;
        this.name = name;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPeople(){
        return tagName.equals("people");
    }
}

