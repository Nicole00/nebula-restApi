/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.utils;

import com.vesoft.nebula.NList;
import com.vesoft.nebula.Path;
import com.vesoft.nebula.Row;
import com.vesoft.nebula.Step;
import com.vesoft.nebula.Tag;
import com.vesoft.nebula.Value;
import com.vesoft.nebula.Vertex;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
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
                String pathStr = getPathString(path);
                if (pathStr == null) {
                    continue;
                }
                results.add(pathStr);
            }
        }
        return results;
    }


    // 输出： 机构名称，法人,担保机构,股东,高管，起点机构的统一社会信用代码,关联路径
    public static List<String> getRowString(ResultSet resultSet) throws UnsupportedEncodingException {
        List<String> rowStrs = new ArrayList<>();
        rowStrs.add("机构名称,法人,担保机构,股东,高管,统一社会信用代码,关联路径");
        for (int i = 0; i < resultSet.rowsSize(); i++) {
            StringBuilder sb = new StringBuilder();
            ResultSet.Record record = resultSet.rowValues(i);

            sb.append(record.get("entname").asString());
            sb.append(",");
            sb.append(getStringList(record.get("faren")));
            sb.append(",");
            sb.append(getStringList(record.get("danbao")));
            sb.append(",");
            sb.append(getStringList(record.get("gudong")));
            sb.append(",");
            sb.append(getStringList(record.get("gaoguan")));
            sb.append(",");
            sb.append(record.get("uniscid").asString());
            sb.append(",");
            Value pathListValue = resultSet.getRows().get(i).values.get(3);
            sb.append(getPathListString(pathListValue));
            rowStrs.add(sb.toString());
        }
        return rowStrs;
    }


    private static String getPathString(Path path) throws UnsupportedEncodingException {
        List<YinLianNode> nodes = new ArrayList<>();
        List<YinLianEdge> edges = new ArrayList<>();

        // 添加path 的start node
        Vertex startVertex = path.getSrc();
        for (Tag tag : startVertex.getTags()) {
            Map<String, Object> properties = new HashMap<>();
            for (Map.Entry<byte[], Value> entry : tag.props.entrySet()) {
                properties.put(new String(entry.getKey(), "utf-8"), entry.getValue().getFieldValue());
            }

            if ((new String(tag.name, "utf-8")).equals("ins")) {
                nodes.add(new YinLianNode("ins", new String((byte[]) properties.get("entname"))));
            }
            if ((new String(tag.name, "utf-8")).equals("people")) {
                nodes.add(new YinLianNode("people", new String((byte[]) properties.get("name_cn"))));
            }
        }

        List<Step> steps = path.getSteps();

        // 添加每个step上的边和点
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            edges.add(new YinLianEdge(step.getType(), new String(step.getName(), "utf-8")));
            Vertex dstVertex = step.getDst();
            for (Tag tag : dstVertex.getTags()) {
                Map<String, Object> properties = new HashMap<>();
                for (Map.Entry<byte[], Value> entry : tag.props.entrySet()) {
                    properties.put(new String(entry.getKey(), "utf-8"), entry.getValue().getFieldValue());
                }
                if ((new String(tag.name, "utf-8")).equals("ins")) {
                    nodes.add(new YinLianNode("ins", new String((byte[]) properties.get("entname"))));
                }
                if ((new String(tag.name, "utf-8")).equals("people")) {
                    nodes.add(new YinLianNode("people", new String((byte[]) properties.get("name_cn"))));
                }
            }
        }

        // 根据nodes 和 edges 拼接结果
        StringBuilder sb = new StringBuilder();
        if (nodes.isEmpty()) {
            return null;
        }
        sb.append(nodes.get(0).getName());
        for (int i = 0; i < edges.size(); i++) {
            sb.append("-");
            boolean relatedPeople = nodes.get(i).isPeople() || nodes.get(i + 1).isPeople();
            sb.append(edges.get(0).getEdgeNameCh(relatedPeople));
            sb.append("-");
            sb.append(nodes.get(i + 1).getName());
        }
        return sb.toString();
    }


    private static String getStringList(ValueWrapper valueWrapper) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (valueWrapper.isList()) {
            List<ValueWrapper> list = valueWrapper.asList();
            for (ValueWrapper value : list) {
                if (value.isString()) {
                    sb.append(value.asString());
                    sb.append("|");
                }
            }
        }
        if(sb.length() >1){
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static String getPathListString(Value pathListValue) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (pathListValue.getSetField() == Value.LVAL) {
            NList paths = (NList) pathListValue.getFieldValue();
            for (Value path : paths.values) {
                if (path.getSetField() == Value.PVAL) {
                    String pathStr = getPathString(path.getPVal());
                    if (pathStr != null) {
                        sb.append(pathStr);
                        sb.append("|");
                    }
                }
            }
            if(sb.length() > 1){
                sb.deleteCharAt(sb.length() - 1);
            }
        }
        return sb.toString();
    }
}
