package com.vesoft.nebula.graph.server.service;

import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.graph.server.exceptions.QueryException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface NebulaGraphService {
    /**
     * connect the NebulaGraph
     *
     * @throws Exception
     */
    Boolean connect();

    /**
     * disconnect the session with NebulaGraph
     */
    void close();


    String queryInsName(String statement) throws QueryException, UnsupportedEncodingException;


    List<String> queryInstrumentRelation(String statement) throws QueryException, UnsupportedEncodingException;

    void save(List<String> results, String filePath, String taskId);

    /**
     * execute ngql
     */
    ResultSet executeNgql(String ngql) throws IOErrorException;


}
