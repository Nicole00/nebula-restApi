package com.vesoft.nebula.graph.server.controller;

import com.vesoft.nebula.Value;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.graph.server.entity.ErrorCode;
import com.vesoft.nebula.graph.server.entity.NebulaQueryResponse;
import com.vesoft.nebula.graph.server.entity.Result;
import com.vesoft.nebula.graph.server.exceptions.QueryException;
import com.vesoft.nebula.graph.server.service.NebulaGraphService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/query/kjsyb_sjfw_up_member_ins_relation_graph")
public class GraphController {
    private static Logger LOG = LoggerFactory.getLogger(GraphController.class);

    @Autowired
    NebulaGraphService nebulaGraphService;

    /**
     * query instrument name according instrument id
     *
     * @param uniscid instrument id
     */
    @GetMapping("/check_ins_id")
    @ResponseBody
    public NebulaQueryResponse queryInsName(@RequestParam String uniscid) {
        LOG.info("Enter GraphController.queryInsName, parameter: uniscid={}", uniscid);

        if (uniscid == null || "".equals(uniscid)) {
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "参数非法", null);
        }

        String statement = String.format("LOOKUP ON ins WHERE ins.uniscid == \"%s\" YIELD ins.entname;", uniscid);

        List<Result> results = new ArrayList<>();
        try {
            List<String> insNames = nebulaGraphService.queryInsName(statement);
            for (String name : insNames) {
                results.add(new Result(name));
            }
        } catch (QueryException e) {
            LOG.error("queryInsName failed, error for execute statement {}, ", statement, e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "请求执行失败:" + e.getMessage(), null);
        } catch (UnsupportedEncodingException e) {
            LOG.error("queryInsName failed for unsupported encoding exception", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "数据编码失败", null);
        }

        return new NebulaQueryResponse(results);
    }


    @GetMapping("/single_ins_relation_rpt")
    @ResponseBody
    public NebulaQueryResponse queryInstrumentRelation(@RequestParam String people_name,
                                                       @RequestParam String ins_id,
                                                       @RequestParam String ins_name,
                                                       @RequestParam int depth,
                                                       @RequestParam int max_out_degree,
                                                       @RequestParam String file_path,
                                                       @RequestParam String task_id) {
        LOG.info("Enter GraphController.queryInstrumentRelation, parameter: " +
                        "people_name={},ins_id={},ins_name{},depth={},max_out_degree={},file_path={},task_id={}",
                people_name, ins_id, ins_name, depth, max_out_degree, file_path, task_id);

        if (ins_id == null || "".equals(ins_id) || depth < 0
                || file_path == null || "".equals(file_path)
                || task_id == null || "".equals(task_id)) {
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "参数非法", null);
        }

        String statement = "";
        List<String> results = new ArrayList<>();
        try {
            results = nebulaGraphService.queryInstrumentRelation(statement);
        } catch (QueryException e) {
            LOG.error("queryInstrumentRelation failed, error for execute statement {}", statement, e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "请求执行失败:" + e.getMessage(), null);
        } catch (UnsupportedEncodingException e) {
            LOG.error("queryInstrumentRelation failed for unsupported encoding exception", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "数据编码失败", null);
        }
        try {
            nebulaGraphService.save(results, file_path, task_id);
        } catch (IOException e) {
            LOG.error("queryInstrumentRelation failed to save result", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "结果写HDFS失败：" + e.getMessage(), null);
        }
        return new NebulaQueryResponse(null);
    }
}
