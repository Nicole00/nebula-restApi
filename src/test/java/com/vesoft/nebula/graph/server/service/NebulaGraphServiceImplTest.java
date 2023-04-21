package com.vesoft.nebula.graph.server.service;

import com.vesoft.nebula.graph.server.data.MockNebulaData;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NebulaGraphServiceImplTest {
    private static Logger LOG = LoggerFactory.getLogger(NebulaGraphServiceImplTest.class);

    @Autowired
    NebulaGraphService service;

    boolean connectFlag = false;

    @Before
    public void setUp() throws Exception {
        MockNebulaData.createSchema();
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testConnect() {
        String addresses = "192.168.15.5:9669,192.168.15.6:9669,192.168.15.7:9669";
        String space = "test";
        try {
            connectFlag = service.connect(addresses, "root", "nebula", space);
        } catch (Exception e) {
            e.printStackTrace();
            assert (false);
        }
        assert (connectFlag);
    }


    @Test
    public void testExecuteNgql() {
        if (!connectFlag) {
            testConnect();
        }
        String statement = "SHOW SPACES";
        ResultSet resultSet = service.executeNgql(statement);
        assert (resultSet.isSucceeded());
    }

    @Test
    public void TestExecuteWithOneBadGraphd() {
        if (!connectFlag) {
            testConnect();
        }
        String statement = "SHOW SPACES";
        List<ResultSet> resultList = Collections.synchronizedList(new ArrayList<>());
        // restart the graphd server during executing the loop
        for (int i = 0; i < 100; i++) {
            ResultSet resultSet = service.executeNgql(statement);
            resultList.add(resultSet);
        }

        assert (resultList.stream().filter(ResultSet::isSucceeded).count() == 100);
    }
}
