/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.entity;


public class YinLianEdge {
    private int typeId;
    private String typeName;

    public YinLianEdge(int typeId, String typeName){
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getEdgeNameCh(boolean relatedPeople){
        String edgeNameCh = null;
        switch (typeName){
            case "is_gudong": {
                if(relatedPeople) {
                    edgeNameCh = "自然人股东边";
                }else{
                    edgeNameCh = "非自然人股东边";
                }
            }
            case "is_gaoguan": edgeNameCh = "高管边";
            case "is_faren": edgeNameCh = "法人边";
            case "danbao_ins": edgeNameCh="担保边";
            case "gudong_ins": edgeNameCh="非自然人股东边";
        }
        if(typeId <0){
            edgeNameCh = edgeNameCh + "(反向)";
        }
        return edgeNameCh;
    }
}

