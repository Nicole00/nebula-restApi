package com.vesoft.nebula.graph.server.service;

import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.graph.server.data.MockNebulaData;
import com.vesoft.nebula.graph.server.service.impl.NebulaGraphServiceImpl;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
        try {
            connectFlag = service.connect();
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
        ResultSet resultSet = null;
        try {
            resultSet = service.executeNgql(statement);
        } catch (IOErrorException e) {
            e.printStackTrace();
            assert(false);
        }
        assert (resultSet.isSucceeded());
    }


    @Test
    public void connectHdfs(){
        try {
            Method getHadoopFsMethod = service.getClass().getDeclaredMethod("getHadoopFs");
            getHadoopFsMethod.setAccessible(true);
            FileSystem fs = (FileSystem) getHadoopFsMethod.invoke(service);
            fs.listStatus(new Path("/"));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IOException e) {
            e.printStackTrace();
            assert(false);
        }
    }
}
