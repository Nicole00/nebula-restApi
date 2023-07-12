package com.vesoft.nebula.graph.server.service.impl;

import com.vesoft.nebula.Row;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.BindSpaceFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.graph.server.exceptions.QueryException;
import com.vesoft.nebula.graph.server.service.NebulaGraphService;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NebulaGraphServiceImpl implements NebulaGraphService {
    private static Logger LOG = LoggerFactory.getLogger(NebulaGraphServiceImpl.class);

    private static SessionPool sessionPool = null;

    @Value("${nebula.graph.servers}")
    private String graphServers;

    @Value("${nebula.user}")
    private String user;

    @Value("${nebula.passwd}")
    private String passwd;

    @Value("${nebula.space}")
    private String space;

    @Value("${nebula.graph.maxSessionSize}")
    private int maxSessionSize = 10;

    @Value("${nebula.graph.minSessionSize}")
    private int minSessionSize = 1;

    @Value("${nebula.graph.timeout}")
    private int timeout = 0;

    // The idleTime for clean the idle session, unit: second
    @Value("${nebula.graph.cleanTime}")
    private int cleanTime = 30;

    // The healthCheckTime for schedule check the health of session, unit: second
    @Value("${nebula.graph.healthyCheckTime}")
    private int healthyCheckTime = 600;

    // retry times for bad session、graphd and storaged server
    @Value("${nebula.graph.retryTimes}")
    private int retryTimes = 10;

    // interval time between retry, unit: millsecond
    @Value("${nebula.graph.intervalTime}")
    private int intervalTime = 100;

    @Value("${hdfs.defaultFS}")
    private String hdfsDefaultFS;

    @Value("${kerberos.user}")
    private String kerberosUser;

    @Value("${keytab.path}")
    private String keytabPath;

    @Value("${krb5.conf.path}")
    private String krb5ConfPath;

    private String timeZoneOffset;

    @Override
    public Boolean connect() {
        if (sessionPool != null) {
            sessionPool.close();
        }
        LOG.info("NebulaGraphService.connect, parameters:graphServers={},user={}", graphServers, user);
        List<HostAddress> addresses = new ArrayList<>();
        for (String host : graphServers.split(",")) {
            String ip = host.split(":")[0];
            int port = Integer.parseInt(host.split(":")[1]);
            addresses.add(new HostAddress(ip, port));
        }

        SessionPoolConfig config = new SessionPoolConfig(addresses, space, user, passwd)
                .setTimeout(timeout)
                .setMaxSessionSize(maxSessionSize)
                .setMinSessionSize(minSessionSize)
                .setHealthCheckTime(healthyCheckTime)
                .setCleanTime(cleanTime)
                .setRetryTimes(retryTimes)
                .setIntervalTime(intervalTime);
        sessionPool = new SessionPool(config);
        return sessionPool.init();
    }

    @Override
    public void close() {
        if (sessionPool != null) {
            sessionPool.close();
        }
    }

    public List<String> queryInsName(String statement) throws QueryException, UnsupportedEncodingException {
        LOG.info("Enter NebulaGraphService.queryInsName, parameter: statement={}", statement);
        ResultSet resultSet = null;
        try {
            resultSet = executeNgql(statement);
        } catch (IOErrorException e) {
            LOG.error("queryInsName error for statement {}", statement, e);
            throw new QueryException("查询执行失败", e);
        }
        if (!resultSet.isSucceeded()) {
            LOG.error("queryInsName failed, failed to execute statement {}, for {}", statement,
                    resultSet.getErrorMessage());
            throw new QueryException("查询执行错误:" + resultSet.getErrorMessage(), null);
        }
        LOG.info("queryInsName success, result row count={}, latency={}",
                resultSet.getRows().size(),
                resultSet.getLatency());

        List<String> insNames = new ArrayList<>();

        if (resultSet.isEmpty()) {
            return insNames;
        }
        List<Row> insRows = resultSet.getRows();
        if (insRows.isEmpty()) {
            return insNames;
        }

        try {
            for (Row ins : insRows) {
                if (ins.values.isEmpty()) {
                    continue;
                }
                insNames.add((new ValueWrapper(ins.values.get(0), "utf-8")).asString());
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("encoding insName failed ", e);
            throw e;
        }
        return insNames;
    }


    @Override
    public List<String> queryInstrumentRelation(String statement) throws QueryException, UnsupportedEncodingException {
        return null;
    }

    @Override
    public void save(List<String> results, String filePath, String taskId) throws IOException {
        FileSystem fs = null;
        try {
            fs = getHadoopFs();
        } catch (IOException e) {
            LOG.error("Connect to HDFS with user={}, keytab.path={}, krb5.conf.path={}, defaultFS={} failed, ",
                    kerberosUser, keytabPath, krb5ConfPath, hdfsDefaultFS, e);
            throw new IOException("HDFS连接失败", e);
        }
        if (filePath.endsWith("/")) {
            filePath = filePath + taskId;
        } else {
            filePath = filePath + "/" + taskId;
        }

        try {
            if (!exist(fs, filePath)) {
                mkdir(fs, filePath);
            }
        } catch (IOException e) {
            LOG.error("check HDFS path or create path with path={} failed", filePath, e);
            throw new IOException("HDFS路径创建失败", e);
        }

        write2Hdfs(results, fs);
    }

    @Override
    public ResultSet executeNgql(String ngql) throws IOErrorException {
        ResultSet resultSet = null;
        try {
            resultSet = sessionPool.execute(ngql);
        } catch (ClientServerIncompatibleException | AuthFailedException | BindSpaceFailedException e) {
            // ignore
        }
        return resultSet;
    }

    private FileSystem getHadoopFs() throws IOException {
        System.setProperty("java.security.krb5.conf", krb5ConfPath);
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsDefaultFS);
        conf.set("hadoop.security.authentication", "kerberos");
        conf.setBoolean("hadoop.security.authorization", true);
        UserGroupInformation.setConfiguration(conf);
        UserGroupInformation.loginUserFromKeytab(kerberosUser, keytabPath);
        FileSystem fs = FileSystem.get(conf);
        return fs;
    }

    private boolean exist(FileSystem fs, String path) throws IOException {
        return fs.exists(new Path(path));
    }

    private boolean mkdir(FileSystem fs, String path) throws IOException {
        return fs.mkdirs(new Path(path));
    }

    private void write2Hdfs(List<String> lines, FileSystem fs) {

    }


}
