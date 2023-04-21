package com.vesoft.nebula.graph.server.service;

import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import org.springframework.stereotype.Service;

@Service
public interface NebulaGraphService {
    /**
     * connect the NebulaGraph
     *
     * @param hosts  NebulaGraph 连接地址
     * @param user   用户名
     * @param passwd 密码
     * @throws Exception
     */
    Boolean connect(String hosts, String user, String passwd,String space);

    /**
     * disconnect the session with NebulaGraph
     */
    void close();


    /**
     * execute ngql
     */
    ResultSet executeNgql(String ngql) throws IOErrorException;
}
