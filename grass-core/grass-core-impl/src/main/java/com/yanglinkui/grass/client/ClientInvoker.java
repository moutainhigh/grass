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

import ch.qos.logback.core.spi.ContextAware;
import com.yanglinkui.grass.*;
import com.yanglinkui.grass.exception.NotFoundException;
import com.yanglinkui.grass.common.Utils;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ClientInvoker implements Invoker {

    private final String spi;

    private final String applicationName;

    private final InvokerAttributes invokerAttributes;

    private final Class<?> spiRouterClass;

    private final Class<?> loadBalanceClass;

    private final boolean isAsync;

    public ClientInvoker(String spi, String applicationName, Class<?> spiRouterClass, Class<?> loadBalanceClass, InvokerAttributes invokerAttributes) {
        if (Utils.isEmpty(spi) && Utils.isEmpty(applicationName)) {
            throw new IllegalArgumentException("spi and application name cannot be both null");
        }

        if (invokerAttributes == null) {
            throw new IllegalArgumentException("invokerAttributes cannot be both null");
        }
        this.spi = spi;
        this.applicationName = applicationName;
        this.invokerAttributes = invokerAttributes;
        this.spiRouterClass = spiRouterClass;
        this.loadBalanceClass = loadBalanceClass;
        this.isAsync = CompletableFuture.class.isAssignableFrom(invokerAttributes.getReturnTypeClass());
    }

    @Override
    public Object invoke(Object[] args) throws Throwable {
        return invoke(args, false);
    }


    public Object invoke(Object[] args, boolean isAsync) throws Throwable {
        GrassRequest request = initRequest(args);

        //调用Template
        Executor<CompletableFuture<GrassResponse>> executor = createClientExecutor();
        CompletableFuture<GrassResponse> response = executor.execute(request);

        ResponseFutureHandler handler = createResponseFutureHandler();
        //TODO: 应该使用指定线程池的版本
        CompletableFuture future = response.handleAsync((result, e) -> {
            return handler.handle(result, e);
        });


        if (this.isAsync || isAsync) {
            return future;
        } else {
            return future.get();
        }
    }

    GrassRequest initRequest(Object[] args) {
        Map<String, Object> parameterMap = new HashMap<>();
        Arrays.stream(this.invokerAttributes.getParameterAttributes()).forEach(a -> {
            parameterMap.put(a.getName(), args[a.getIndex()]);
        });

        //基础信息
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setSpi(this.spi);
        request.setApplicationName(this.applicationName);
        request.setServiceId(this.invokerAttributes.getId());
        request.setRequestTime(System.currentTimeMillis());
        request.setBody(Collections.unmodifiableMap(parameterMap));

        return request;
    }

    protected Executor<CompletableFuture<GrassResponse>> createClientExecutor() {
        ClientExecutorBuilder builder = GrassContextManager.getContext().getClientExecutorBuilder();
        builder.setLoadBalance(getLoadBalance());
        builder.setLoadBalance(getLoadBalance());
        return builder
                .setLoadBalance(getLoadBalance())
                .setSpiRouter(getSpiRouter())
                .build();
    }

    LoadBalance getLoadBalance() {
        LoadBalance loadBalance = null;
        if (this.loadBalanceClass != void.class && this.loadBalanceClass != null) {
            loadBalance = Optional.ofNullable((LoadBalance) GrassContextManager.getContext().getInstance(this.loadBalanceClass)).orElseThrow(
                    () -> new NotFoundException("Not found LoadBalance: " + this.loadBalanceClass)
            );
        }

        return loadBalance;
    }

    SpiRouter getSpiRouter() {
        SpiRouter spiRouter = null;

        if (this.spiRouterClass != void.class && this.spiRouterClass != null) {
            spiRouter = Optional.ofNullable((SpiRouter) GrassContextManager.getContext().getInstance(this.spiRouterClass)).orElseThrow(
                    () -> new NotFoundException("Not found SpiRouter: " + this.spiRouterClass)
            );
        }

        return spiRouter;
    }

    protected ResponseFutureHandler createResponseFutureHandler() {
        return new ResponseFutureHandler(this.invokerAttributes.getReturnType());
    }

    @Override
    public InvokerAttributes getAttributes() {
        return this.invokerAttributes;
    }
}
