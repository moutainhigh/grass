/*
 * Copyright 2019 Jonas Yang
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
 */

package com.yanglinkui.grass.client;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.client.processor.*;
import com.yanglinkui.grass.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ClientExecutor implements Executor<CompletableFuture<GrassResponse>> {

    private final GrassContext context;

    private final String instanceId;

    private final SpiRouter spiRouter;

    private final LoadBalance loadBalance;

    private final Registry registry;

    private final List<ClientProcessor> clientProcessorList;

    private final List<Router> routerList;

    private final long timeout;


    public ClientExecutor(GrassContext context, LoadBalance loadBalance, SpiRouter spiRouter, long timeout, Registry registry) {
        this.context = context;
        this.instanceId = context.getApplicationInstanceId();
        this.loadBalance = Optional.ofNullable(loadBalance).orElseGet(() -> context.getDefaultLoadBalance());
        this.spiRouter = Optional.ofNullable(spiRouter).orElseGet(() -> context.getDefaultSpiRouter());
        this.registry = registry;
        this.routerList = context.getRouterList();
        this.clientProcessorList = context.getClientProcessorList();
        this.timeout = timeout;
    }

    @Override
    public CompletableFuture<GrassResponse> execute(GrassRequest request) {
        request = new GrassRequestWrapper(request) {
            @Override
            public String getRemoteUser() {
                return instanceId;
            }
        };

        List<ClientProcessor> processorList = new ArrayList<>();
        processorList.addAll(this.clientProcessorList);

        //ending processors
        processorList.add(new SpiProcessor(this.spiRouter, registry));
        processorList.add(new RouterProcessor(this.routerList, registry));

        processorList.add(new LoadBalanceProcessor(this.loadBalance));
        processorList.add(new CallServiceProcessor(this.context, this.timeout));

        ClientProcessorChain interceptChain = getClientProcessorChain(request, processorList);
        return interceptChain.proceed(request);
    }

    protected ClientProcessorChain getClientProcessorChain(GrassRequest request, List<ClientProcessor> processorList) {
        return new ClientProcessorChain(request, processorList);
    }

}
