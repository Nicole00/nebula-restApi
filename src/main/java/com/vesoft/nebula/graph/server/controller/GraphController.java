package com.vesoft.nebula.graph.server.controller;

import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.graph.server.entity.ErrorCode;
import com.vesoft.nebula.graph.server.entity.NebulaConnectResponse;
import com.vesoft.nebula.graph.server.entity.NebulaQueryResponse;
import com.vesoft.nebula.graph.server.service.NebulaGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

// 返回结果可以只是 count
@RestController
public class GraphController {
    private static Logger LOG = LoggerFactory.getLogger(GraphController.class);

    @Autowired
    NebulaGraphService nebulaGraphService;

    /**
     * prepare NebulaGraph SessionPool
     */
    @GetMapping("/connect")
    @ResponseBody
    public NebulaConnectResponse connectNebulaGraph(@RequestParam String host,
                                                    @RequestParam String user,
                                                    @RequestParam String passwd,
                                                    @RequestParam String space) {
        LOG.info("enter NebulaGraphController.connectNebulaGraph");

        NebulaConnectResponse response = new NebulaConnectResponse();

        if (host == null || host.trim().isEmpty()) {
            LOG.error("host is invalid, your host is {}", host);
            response.setResp(ErrorCode.PARAM_ERROR);
            return response;
        }
        if (user == null || user.trim().isEmpty() || passwd == null || passwd.trim().isEmpty()) {
            LOG.error("user or password is invalid, your user is {},password is {}", user, passwd);
            response.setResp(ErrorCode.PARAM_ERROR);
            return response;
        }

        String[] hosts = host.split(",");
        for (String address : hosts) {
            String[] ipAndPort = address.split(":");
            if (ipAndPort.length < 2) {
                LOG.error("host {} has wrong format", host);
                response.setResp(ErrorCode.PARAM_ERROR);
                return response;
            }
            if (Integer.parseInt(ipAndPort[1]) <= 0 || Integer.parseInt(ipAndPort[1]) >= 65535) {
                LOG.error("port {} out of range.", ipAndPort[1]);
                response.setResp(ErrorCode.PARAM_ERROR);
                return response;
            }
        }

        try {
            Boolean connected = nebulaGraphService.connect(host, user, passwd, space);
            if (connected) {
                response.setResp(ErrorCode.SUCCESS);
            } else {
                response.setResp(ErrorCode.SESSION_POOL_INIT_ERROR);
            }

        } catch (Exception e) {
            LOG.error("NebulaGraphController.connectNebulaGraph error: ", e);
            response.setResp(ErrorCode.SESSION_POOL_INIT_ERROR);
            return response;
        }
        LOG.info("connectNebulaGraph response: {}", response.toString());
        return response;
    }

    /**
     * close NebulaGraph SessionPool
     */
    @GetMapping("/close")
    public NebulaConnectResponse close() {
        nebulaGraphService.close();
        NebulaConnectResponse response = new NebulaConnectResponse();
        response.setResp(ErrorCode.SUCCESS);
        return response;
    }


    /**
     * insert vertex
     */
    @PostMapping("/insertVertex")
    @ResponseBody
    public NebulaQueryResponse insertVertex(@RequestParam String uid,
                                            @RequestParam String birthday,
                                            @RequestParam String firstName,
                                            @RequestParam String lastName,
                                            @RequestParam String gender,
                                            @RequestParam String language,
                                            @RequestParam String browserUsed,
                                            @RequestParam String locationIp,
                                            @RequestParam String creationDate) {
        String statement = String.format(
                "INSERT VERTEX Person(birthday, firstName, lastName, gender, language, " +
                        "browserUsed, locationIP, creationDate) values \"%s\":(date(\"%s\"), " +
                        "\"%s\", \"%s\", \"%s\", \"%s\", \"%s\", \"%s\", datetime(\"%s\"))",
                uid, birthday, firstName, lastName, gender, language, browserUsed, locationIp,
                creationDate);
        return execute(statement);
    }


    /**
     * insert edge
     */
    @GetMapping("/insertEdge")
    @ResponseBody
    public NebulaQueryResponse insertEdge(@RequestParam String srcUid,
                                          @RequestParam String dstUid,
                                          @RequestParam String creationDate,
                                          @RequestParam(required = false) Long rank) {
        long edgeRank = 0;
        if (rank != null) {
            edgeRank = rank;
        }
        String statement = String.format(
                "INSERT EDGE knows(creationDate) values " +
                        "\"%s\"->\"%s\"@%d:(date(\"%s\"))", srcUid, dstUid, edgeRank, creationDate);
        return execute(statement);
    }


    /**
     * update vertex
     */
    @PostMapping("/updateVertex")
    @ResponseBody
    public NebulaQueryResponse updateVertex(@RequestParam String uid,
                                            @RequestParam String browserUsed) {
        String statement = String.format(
                "UPDATE VERTEX on Person %s SET browserUsed=\"%s\";", uid, browserUsed);
        return execute(statement);
    }


    private NebulaQueryResponse execute(String statement) {
        NebulaQueryResponse response = new NebulaQueryResponse();
        ResultSet resultSet = null;
        try {
            resultSet = nebulaGraphService.executeNgql(statement);
        } catch (IOErrorException e) {
            LOG.error("Controller-> execute failed, ", e);
            response.setResp(ErrorCode.ERROR);
            return response;
        }

        if (resultSet == null) {
            response.setResp(ErrorCode.ERROR);
        } else {
            response.setRowCount(resultSet.rowsSize());
            response.setLatency(resultSet.getLatency());
            response.setCode(resultSet.getErrorCode());
            response.setMessage(resultSet.getErrorMessage());
        }
        return response;
    }

}
