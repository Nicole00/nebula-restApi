/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.utils;

import com.vesoft.nebula.Path;
import com.vesoft.nebula.Row;
import com.vesoft.nebula.Step;
import com.vesoft.nebula.Tag;
import com.vesoft.nebula.Value;
import com.vesoft.nebula.Vertex;
import com.vesoft.nebula.graph.server.entity.YinLianEdge;
import com.vesoft.nebula.graph.server.entity.YinLianNode;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResolvePath {

    public static List<String> getPathString(List<Row> rows) throws UnsupportedEncodingException {
        List<String> results = new ArrayList<>();
        for (Row row : rows) {
            com.vesoft.nebula.Value value = row.values.get(0);
            if (value.getSetField() == Value.PVAL) {
                Path path = value.getPVal();

                List<YinLianNode> nodes = new ArrayList<>();
                List<YinLianEdge> edges = new ArrayList<>();

                // 添加path 的start node
                Vertex startVertex = path.getSrc();
                for (Tag tag : startVertex.getTags()) {
                    Map<String, Object> properties = new HashMap<>();
                    for(Map.Entry<byte[], Value> entry: tag.props.entrySet()){
                        properties.put(new String(entry.getKey(), "utf-8"), entry.getValue().getFieldValue());
                    }

                    if ((new String(tag.name, "utf-8")).equals("ins")) {
                        nodes.add(new YinLianNode("ins", new String((byte[])properties.get("entname"))));
                    }
                    if ((new String(tag.name, "utf-8")).equals("people")) {
                        nodes.add(new YinLianNode("people",new String((byte[])properties.get("name_cn"))));
                    }
                }

                List<Step> steps = path.getSteps();

                // 添加每个step上的边和点
                for (int i = 0; i < steps.size(); i++) {
                    Step step = steps.get(i);
                    edges.add(new YinLianEdge(step.getType(), new String(step.getName(), "utf-8")));
                    Vertex dstVertex = step.getDst();
                    for (Tag tag : startVertex.getTags()) {
                        Map<String, Object> properties = new HashMap<>();
                        for(Map.Entry<byte[], Value> entry: tag.props.entrySet()){
                            properties.put(new String(entry.getKey(), "utf-8"), entry.getValue().getFieldValue());
                        }
                        if ((new String(tag.name, "utf-8")).equals("ins")) {
                            nodes.add(new YinLianNode("ins", new String((byte[])properties.get("entname"))));
                        }
                        if ((new String(tag.name, "utf-8")).equals("people")) {
                            nodes.add(new YinLianNode("people",new String((byte[])properties.get("name_cn"))));
                        }
                    }
                }

                // 根据nodes 和 edges 拼接结果
                StringBuilder sb = new StringBuilder();
                if(nodes.isEmpty()){
                    continue;
                }
                sb.append(nodes.get(0).getName());
                for(int i=0;i<edges.size();i++){
                    sb.append("-");
                    boolean relatedPeople = nodes.get(i).isPeople() || nodes.get(i+1).isPeople();
                    sb.append(edges.get(0).getEdgeNameCh(relatedPeople));
                    sb.append("-");
                    sb.append(nodes.get(i+1).getName());
                }
                results.add(sb.toString());
            }
        }
        return results;
    }
}
