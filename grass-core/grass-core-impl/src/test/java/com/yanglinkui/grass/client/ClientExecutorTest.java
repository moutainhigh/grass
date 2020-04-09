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
import com.yanglinkui.grass.mock.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
public class ClientExecutorTest {

    public static final String zone = "test-zone";

    public static final String application = "test-app";

    MockRegistry registry;

    List<ApplicationInstance> applicationInstanceList;

    CompletableFuture<GrassResponse> responseCompletableFuture;
    MockProtocolClient client;

    List<Router> routerList;

    @BeforeEach
    public void init() {
        applicationInstanceList = new ArrayList<>();
        applicationInstanceList.add(new MockDefaultApplicationInstance(zone, application, MockProtocolFactory.ID));

        responseCompletableFuture = new CompletableFuture<>();
        client = new MockProtocolClient(responseCompletableFuture);


        registry = new MockRegistry();
        registry.setApplicationInstanceList(applicationInstanceList);

        routerList = new ArrayList<>();
        routerList.add(new MockRouter(applicationInstanceList));
    }

    @Test
    public void testExecute(@Mock GrassContext context) {
        when(context.getProtocol("message")).thenReturn(client);
        when(context.getRouterList()).thenReturn(routerList);

        DefaultClientExecutorBuilder builder = new DefaultClientExecutorBuilder(context, registry);
        Executor<CompletableFuture<GrassResponse>> executor = builder.setSpiRouter((spi, applicationList) -> application)
                .setLoadBalance((zone, application, applicationInstanceList) -> applicationInstanceList.get(0))
                .build();

        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setZone(zone);
        request.setSpi(applicationInstanceList.get(0).getSpi());


        CompletableFuture<GrassResponse> result = executor.execute(request);
        assertEquals(responseCompletableFuture, result);
    }


}
