/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.controller;

import com.vesoft.nebula.graph.server.service.NebulaGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class NebulaApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private static Logger LOG = LoggerFactory.getLogger(NebulaApplicationListener.class);

    @Autowired
    NebulaGraphService graphService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if(!graphService.connect()){
            LOG.error("failed to connect NebulaGraph, stopping server.....");
            event.getApplicationContext().close();
        }
    }
}
