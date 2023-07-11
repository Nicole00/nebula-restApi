/* Copyright (c) 2023 vesoft inc. All rights reserved.
 *
 * This source code is licensed under Apache 2.0 License.
 */

package com.vesoft.nebula.graph.server.controller;

import com.vesoft.nebula.graph.server.service.NebulaGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class NebulaApplicationCloseListener implements ApplicationListener<ContextClosedEvent> {
    private static Logger LOG = LoggerFactory.getLogger(NebulaApplicationCloseListener.class);

    @Autowired
    NebulaGraphService graphService;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        LOG.info("Spring server will be closed, closing NebulaGraph Session first.....");
        graphService.close();
    }
}
