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

package com.yanglinkui.grass.client.processor;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.DefaultGrassRequest;
import com.yanglinkui.grass.mock.MockLoadBalance;
import com.yanglinkui.grass.mock.MockDefaultApplicationInstance;
import com.yanglinkui.grass.mock.MockProcessorChain;
import com.yanglinkui.grass.DefaultGrassResponse;
import com.yanglinkui.grass.mock.MockProtocolFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LoadBalanceProcessorTest {

    MockLoadBalance loadBalance;

    Processor.Chain<CompletableFuture<GrassResponse>> chain;

    private String zone = "zone-test";

    private String application = "app3";

    private List<ApplicationInstance> applicationInstanceList = new ArrayList<>();

    private DefaultGrassRequest request;

    @BeforeEach
    public void init() {
        ApplicationInstance instance = new MockDefaultApplicationInstance(zone, application, MockProtocolFactory.ID);
        applicationInstanceList.add(instance);

        loadBalance = new MockLoadBalance(0);

        request = new DefaultGrassRequest();
        request.setZone(zone);
        request.setApplicationName(application);
        request.setAttribute(ProcessorConstants.Request.Attribute.APPLICATION_INSTANCE_LIST, applicationInstanceList);

        chain = new MockProcessorChain<>(request, CompletableFuture.completedFuture(new DefaultGrassResponse()));

    }

    @Test
    public void testProcess() {
        LoadBalanceProcessor processor = new LoadBalanceProcessor(loadBalance);
        processor.process(chain);

        ApplicationInstance instance = request.getAttribute(ProcessorConstants.Request.Attribute.APPLICATION_INSTANCE);
        Assertions.assertEquals(applicationInstanceList.get(0), instance);
    }
}
