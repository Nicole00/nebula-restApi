package com.vesoft.nebula.graph.server.service.impl;

import com.vesoft.nebula.ErrorCode;
import com.vesoft.nebula.client.graph.SessionPool;
import com.vesoft.nebula.client.graph.SessionPoolConfig;
import com.vesoft.nebula.client.graph.data.HostAddress;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.AuthFailedException;
import com.vesoft.nebula.client.graph.exception.BindSpaceFailedException;
import com.vesoft.nebula.client.graph.exception.ClientServerIncompatibleException;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.graph.server.service.NebulaGraphService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NebulaGraphServiceImpl implements NebulaGraphService {
    private static Logger LOG = LoggerFactory.getLogger(NebulaGraphServiceImpl.class);

    private static SessionPool sessionPool = null;

    @Value("${nebula.graph.maxConnSize}")
    private int maxConnSize = 10;

    @Value("${nebula.graph.minConnSize}")
    private int minConnSize = 1;

    @Value("${nebula.graph.timeout}")
    private int timeout = 0;

    // The idleTime for clean the idle session, unit: second
    @Value("${nebula.graph.cleanTime}")
    private int cleanTime = 30;

    // The healthCheckTime for schedule check the health of session, unit: second
    @Value("${nebula.graph.healthyCheckTime}")
    private int healthyCheckTime = 600;

    // retry times for bad graphd and storaged server
    @Value("${nebula.graph.retryTimes}")
    private int retryTimes = 10;

    // interval time between retry, unit: millsecond
    @Value("${nebula.graph.intervalTime}")
    private int intervalTime = 100;

    private String timeZoneOffset;

    @Override
    public Boolean connect(String hosts, String user, String passwd, String space) {
        if (sessionPool != null) {
            sessionPool.close();
        }
        LOG.info("NebulaGraphService.connect, parameters:hosts={},user={},passwd={}", hosts, user
                , passwd);
        List<HostAddress> addresses = new ArrayList<>();
        for (String host : hosts.split(",")) {
            String ip = host.split(":")[0];
            int port = Integer.parseInt(host.split(":")[1]);
            addresses.add(new HostAddress(ip, port));
        }

        SessionPoolConfig config = new SessionPoolConfig(addresses, space, user, passwd)
                .setTimeout(timeout)
                .setMaxSessionSize(maxConnSize)
                .setMinSessionSize(minConnSize)
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


    @Override
    public ResultSet executeNgql(String ngql) throws IOErrorException {
        ResultSet resultSet = null;
        try {
            resultSet = sessionPool.execute(ngql);
        } catch (ClientServerIncompatibleException | AuthFailedException | BindSpaceFailedException e) {
            // ignore
        } catch (IOErrorException e) {
            LOG.error("execute failed, ", e);
            throw e;
        }
        return resultSet;
    }
}
