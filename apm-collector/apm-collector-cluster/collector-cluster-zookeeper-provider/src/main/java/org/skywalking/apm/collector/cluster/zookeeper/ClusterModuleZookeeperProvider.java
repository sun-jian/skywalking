/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.cluster.zookeeper;

import java.util.Properties;
import org.skywalking.apm.collector.client.zookeeper.ZookeeperClient;
import org.skywalking.apm.collector.cluster.ClusterModule;
import org.skywalking.apm.collector.cluster.service.ModuleListenerService;
import org.skywalking.apm.collector.cluster.service.ModuleRegisterService;
import org.skywalking.apm.collector.cluster.service.ModuleRegistrationGetService;
import org.skywalking.apm.collector.cluster.zookeeper.service.ZookeeperModuleListenerService;
import org.skywalking.apm.collector.cluster.zookeeper.service.ZookeeperModuleRegisterService;
import org.skywalking.apm.collector.cluster.zookeeper.service.ZookeeperModuleRegistrationGetService;
import org.skywalking.apm.collector.core.CollectorException;
import org.skywalking.apm.collector.core.UnexpectedException;
import org.skywalking.apm.collector.core.module.Module;
import org.skywalking.apm.collector.core.module.ModuleProvider;
import org.skywalking.apm.collector.core.module.ServiceNotProvidedException;

/**
 * @author peng-yongsheng
 */
public class ClusterModuleZookeeperProvider extends ModuleProvider {

    private static final String HOST_PORT = "hostPort";
    private static final String SESSION_TIMEOUT = "sessionTimeout";

    private ClusterZKDataMonitor dataMonitor;

    @Override public String name() {
        return "zookeeper";
    }

    @Override public Class<? extends Module> module() {
        return ClusterModule.class;
    }

    @Override public void prepare(Properties config) throws ServiceNotProvidedException {
        dataMonitor = new ClusterZKDataMonitor();

        final String hostPort = config.getProperty(HOST_PORT);
        final int sessionTimeout = (Integer)config.get(SESSION_TIMEOUT);
        ZookeeperClient zookeeperClient = new ZookeeperClient(hostPort, sessionTimeout, dataMonitor);
        dataMonitor.setClient(zookeeperClient);

        this.registerServiceImplementation(ModuleListenerService.class, new ZookeeperModuleListenerService(dataMonitor));
        this.registerServiceImplementation(ModuleRegisterService.class, new ZookeeperModuleRegisterService(dataMonitor));
        this.registerServiceImplementation(ModuleRegistrationGetService.class, new ZookeeperModuleRegistrationGetService());
    }

    @Override public void start(Properties config) throws ServiceNotProvidedException {
        try {
            dataMonitor.start();
        } catch (CollectorException e) {
            throw new UnexpectedException(e.getMessage());
        }
    }

    @Override public void notifyAfterCompleted() throws ServiceNotProvidedException {

    }

    @Override public String[] requiredModules() {
        return new String[0];
    }

}