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
     * @param ins_id 机构的统一社会信用代码，即uniscid 的值
     */
    @GetMapping("/check_ins_id")
    @ResponseBody
    public NebulaQueryResponse queryInsName(@RequestParam String ins_id) {
        LOG.info("Enter GraphController.queryInsName, parameter: ins_id={}", ins_id);

        if (ins_id == null || "".equals(ins_id)) {
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "参数非法", null);
        }

        String statement = String.format("LOOKUP ON ins WHERE ins.uniscid == \"%s\" YIELD ins.entname;", ins_id);

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


    // 算法2
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

        if (ins_id == null || "".equals(ins_id) || depth < 0 || max_out_degree < 0
                || file_path == null || "".equals(file_path)
                || task_id == null || "".equals(task_id)) {
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "参数非法", null);
        }

        String getSrcStatement = String.format("$src = lookup on ins where ins.uniscid == \"%s\" YIELD id(vertex) as " +
                "vid;", ins_id);
        String getDstStatement = String.format("$dst = get subgraph with prop %d steps from $src.vid both is_gudong, " +
                "is_gaoguan, is_faren, danbao_ins, gudong_ins yield vertices as v | unwind $-.v as v | yield id($-.v)" +
                " as vid where \"ins\" in tags($-.v) and id($-.v)<>\"id_\";", depth);
        String getPath = String.format("$p = find noloop path with prop from $src.vid to $dst.vid over * bidirect " +
                "upto %d steps yield path as p | yield $-.p as p where all (x in nodes($-.p) where x.statistics" +
                ".out_degree<%d) | yield id(last(nodes($-.p))) as l, $-.p as p | group by $-.l yield $-.l as l, " +
                "collect($-.p) as p;", depth, max_out_degree);
        String getAllInfo = "$r = go from $p.l over is_gudong, is_gaoguan, is_faren, danbao_ins, gudong_ins reversely" +
                " " +
                "yield id($^) as src, $^.ins.entname as entname, $^.ins.uniscid as uniscid, $p.p as all_path," +
                "case type(edge) when \"is_faren\" then $$.people.name_cn + \"（\" + is_faren.title_desc + \"）\" else " +
                "null end as faren, " +
                "case type(edge) when \"is_gaoguan\" then $$.people.name_cn + \"（\" + is_gaoguan.title_desc + \"）\" " +
                "else null end as gaoguan," +
                "case type(edge) when \"danbao_ins\" then $$.ins.entname + \"（担保机构）\" else null end as danbao," +
                "case type(edge) when \"gudong_ins\" then " +
                "case when \"people\" in tags($$) then $$.people.name_cn + \"（自然人股东）\" else $$.ins.entname + " +
                "\"（非自然人股东）\" end else null end as gudong " +
                "| group by $-.src yield $-.src as src, max($-.entname) as entname, max($-.uniscid) as uniscid, max" +
                "($-.all_path) as all_path, collect($-.faren) as faren, collect($-.gaoguan) as gaoguan, collect($-" +
                ".danbao) as danbao, collect($-.gudong) as gudong;";


        String statement = getSrcStatement + getDstStatement + getPath + getAllInfo;
        List<String> results;
        try {
            results = nebulaGraphService.queryInstrumentRelation(statement);
        } catch (QueryException e) {
            LOG.error("queryInstrumentRelation failed, error for execute statement {}", statement, e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "请求执行失败:" + e.getMessage(), null);
        } catch (UnsupportedEncodingException e) {
            LOG.error("queryInstrumentRelation failed for unsupported encoding exception", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "数据编码失败", null);
        }
        if (results == null) {
            LOG.info("result is empty.");
            return new NebulaQueryResponse(new ArrayList<Result>());
        }

        try {
            nebulaGraphService.save(results, file_path, task_id);
        } catch (IOException e) {
            LOG.error("queryInstrumentRelation failed to save result", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "结果写HDFS失败:" + e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            LOG.error("queryInstrumentRelation failed to create path", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "创建文件失败:" + e.getMessage(), null);
        }
        return new NebulaQueryResponse(new ArrayList<Result>());
    }


    // 算法3
    @GetMapping("/double_ins_relation_rpt")
    @ResponseBody
    public NebulaQueryResponse queryInstrumentsRelation(@RequestParam String people_name_start,
                                                        @RequestParam String ins_id_start,
                                                        @RequestParam String people_name_end,
                                                        @RequestParam String ins_id_end,
                                                        @RequestParam int depth,
                                                        @RequestParam int max_out_degree,
                                                        @RequestParam String file_path,
                                                        @RequestParam String task_id) {
        LOG.info("Enter GraphController.queryInstrumentRelation, parameter: " +
                        "people_name_start={},ins_id_start={},people_name_end{},ins_id_end={}" +
                        ",depth={},max_out_degree={},file_path={},task_id={}",
                people_name_start, ins_id_start, people_name_end, ins_id_end,
                depth, max_out_degree, file_path, task_id);

        if (ins_id_start == null || "".equals(ins_id_start) || depth < 0
                || ins_id_end == null || "".equals(ins_id_end)
                || file_path == null || "".equals(file_path)
                || task_id == null || "".equals(task_id)) {
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "参数非法", null);
        }

        String statement = String.format("$src = lookup on ins where ins.uniscid ==\"%s\" YIELD id(vertex) as vid;" +
                        "$dst = lookup on ins where ins.uniscid ==\"%s\" YIELD id(vertex) as vid;" +
                        "find noloop path with prop from $src.vid to $dst.vid over * bidirect upto %d steps yield " +
                        "path as p ",
                ins_id_start, ins_id_end, depth);
        if (max_out_degree > 0) {
            String maxOutDegreeStatement = String.format(" | yield $-.p as p where all (x in nodes($-.p) where x" +
                    ".statistics.out_degree<%d);", max_out_degree);
            statement = statement + maxOutDegreeStatement;
        }

        List<String> results;
        try {
            results = nebulaGraphService.queryInstrumentsRelation(statement);
        } catch (QueryException e) {
            LOG.error("queryInstrumentRelation failed, error for execute statement {}", statement, e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "请求执行失败:" + e.getMessage(), null);
        } catch (UnsupportedEncodingException e) {
            LOG.error("queryInstrumentRelation failed for unsupported encoding exception", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "数据编码失败", null);
        }
        if (results == null) {
            LOG.info("result is empty.");
            return new NebulaQueryResponse(new ArrayList<Result>());
        }

        try {
            nebulaGraphService.save(results, file_path, task_id);
        } catch (IOException e) {
            LOG.error("queryInstrumentsRelation failed to save result", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "结果写HDFS失败：" + e.getMessage(), null);
        } catch (IllegalArgumentException e) {
            LOG.error("queryInstrumentsRelation failed to create path", e);
            return new NebulaQueryResponse(ErrorCode.ERROR.getErrorCode(), "创建文件失败:" + e.getMessage(), null);
        }
        return new NebulaQueryResponse(new ArrayList<Result>());
    }
}
