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

package com.yanglinkui.grass.provider;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.Error;
import com.yanglinkui.grass.provider.processor.*;

import java.util.ArrayList;
import java.util.List;

public class ServiceExecutor implements Executor<GrassResponse> {

    private final GrassContext context;

    private final Repository<Service> repository;

    public ServiceExecutor(GrassContext context, Repository<Service> repository) {
        this.context = context;
        this.repository = repository;
    }

    @Override
    public GrassResponse execute(GrassRequest request) {
        long startedTime = System.currentTimeMillis();

        List<ServiceProcessor> sourceList = this.context.getServiceProcessorList();

        List<ServiceProcessor> processorList = new ArrayList<>();
        processorList.addAll(sourceList);
        processorList.add(new CheckRequestProcessor(this.context.getZone(), this.context.getApplicationName()));
        processorList.add(new FindServiceProcessor(this.repository));
        processorList.add(new DecodeParameterProcessor());
        processorList.add(new CallInvokerProcessor());
        ServiceProcessorChain chain = new ServiceProcessorChain(request, processorList);

        GrassResponse response = null;
        try {
            response = chain.proceed(request);
        } catch (Throwable e) {
            Error error = new Error(e);
            DefaultGrassResponse errorResponse = new DefaultGrassResponse();
            errorResponse.setStatus(error.getCode());
            errorResponse.setBody(error);

            response = errorResponse;
        }

        return fillResponse(response, request.getId(), startedTime, System.currentTimeMillis());
    }

    private GrassResponseWrapper fillResponse(GrassResponse response, final String  correlationId, final long startedTime, final long returnedTime) {
        return new GrassResponseWrapper(response) {
            @Override
            public String getCorrelationId() {
                return correlationId;
            }

            @Override
            public String getApplicationName() {
                return context.getApplicationName();
            }

            @Override
            public String getInstanceId() {
                return context.getApplicationInstanceId();
            }

            @Override
            public String getZone() {
                return context.getZone();
            }

            @Override
            public Long getInvokedTime() {
                return startedTime;
            }

            @Override
            public Long getReturnedTime() {
                return returnedTime;
            }
        };
    }
}
