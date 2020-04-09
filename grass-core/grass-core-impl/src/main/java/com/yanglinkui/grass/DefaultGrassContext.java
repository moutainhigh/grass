/**
 * Copyright 2019 Jonas Yang
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yanglinkui.grass;

import com.yanglinkui.grass.client.*;

import com.yanglinkui.grass.protocol.Client;
import com.yanglinkui.grass.protocol.ProtocolFactory;
import com.yanglinkui.grass.protocol.ProtocolFactoryManager;
import com.yanglinkui.grass.provider.DefaultServiceExecutorBuilder;
import com.yanglinkui.grass.provider.ServiceExecutorBuilder;
import com.yanglinkui.grass.provider.ServiceProcessor;
import com.yanglinkui.grass.provider.ServiceRepository;
import com.yanglinkui.grass.registry.Registry;
import com.yanglinkui.grass.serialize.Serializer;
import com.yanglinkui.grass.serialize.SerializerFactory;
import com.yanglinkui.grass.serialize.SerializerFactoryManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DefaultGrassContext implements GrassContext {

    protected final DefaultObjectFactory objectFactory;

    protected final ProtocolFactoryManager protocolFactoryManager;

    protected final SerializerFactoryManager serializerFactoryManager;

    protected final List<ClientProcessor> clientProcessorList;

    protected final List<ServiceProcessor> serviceProcessorList;

    protected final List<Router> routerList;

    protected final LoadBalance defaultLoadBalance;

    protected final SpiRouter defaultSpiRouter;

    protected final GrassSetting setting;

    protected final Registry registry;

    public DefaultGrassContext(GrassSetting setting, List<ObjectFactory> objectFactoryList, ProtocolFactoryManager protocolFactoryManager,
                               SerializerFactoryManager serializerFactoryManager,
                               List<ClientProcessor> clientProcessorList, List<ServiceProcessor> serviceProcessorList,
                               List<Router> routerList, LoadBalance defaultLoadBalance,
                               SpiRouter defaultSpiRouter,
                               ServiceRepository serviceRepository,
                               Registry registry) {
        this.setting = setting;
        this.objectFactory = new DefaultObjectFactory(objectFactoryList);
        this.protocolFactoryManager = protocolFactoryManager;
        this.serializerFactoryManager = serializerFactoryManager;
        this.clientProcessorList = clientProcessorList;
        this.serviceProcessorList = serviceProcessorList;
        this.routerList = routerList;
        this.defaultLoadBalance = defaultLoadBalance;
        this.defaultSpiRouter = defaultSpiRouter;;
        this.registry = registry;
    }

    @Override
    public <T> T getInstance(Class<?> clazz, String id) {
        return this.objectFactory.getInstance(clazz, id);
    }

    @Override
    public <T> T getInstance(Class<?> clazz) {
        return this.objectFactory.getInstance(clazz);
    }

    @Override
    public <T> T getInstance(String id) {
        return this.objectFactory.getInstance(id);
    }

    public void addInstance(String id, Object object) {
        this.objectFactory.addInstance(id, object);
    }

    @Override
    public Client getProtocol(String id) {
        ProtocolFactory factory = this.protocolFactoryManager.getProtocolFactory(id);
        if (factory == null) {
            return null;
        }

        return factory.getClient();
    }

    @Override
    public Serializer getSerializer(String id) {
        SerializerFactory factory = this.serializerFactoryManager.getSerializerFactory(id);
        if (factory == null) {
            return null;
        }

        return factory.getSerializer();
    }

    @Override
    public List<ClientProcessor> getClientProcessorList() {
        return Optional.ofNullable(this.clientProcessorList).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public List<ServiceProcessor> getServiceProcessorList() {
        return Optional.ofNullable(this.serviceProcessorList).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public List<Router> getRouterList() {
        return Optional.ofNullable(this.routerList).orElse(Collections.EMPTY_LIST);
    }

    @Override
    public LoadBalance getDefaultLoadBalance() {
        return this.defaultLoadBalance;
    }

    @Override
    public ClientExecutorBuilder getClientExecutorBuilder() {
        return new DefaultClientExecutorBuilder(this, this.registry);
    }

    @Override
    public SpiRouter getDefaultSpiRouter() {
        return this.defaultSpiRouter;
    }

    @Override
    public ServiceExecutorBuilder getServiceExecutorBuilder() {
        return new DefaultServiceExecutorBuilder(this);
    }

    @Override
    public String getZone() {
        return this.setting.getZone();
    }

    @Override
    public String getApplicationName() {
        return this.setting.getApplicationName();
    }

    @Override
    public String getSpi() {
        return this.setting.getSpi();
    }

    @Override
    public String getApplicationInstanceId() {
        return this.setting.getInstanceId();
    }

    @Override
    public String getApplicationMetadata(String name) {
        return this.setting.getMetadata(name);
    }

}
