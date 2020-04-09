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
import static org.junit.jupiter.api.Assertions.*;

import com.yanglinkui.grass.mock.*;
import com.yanglinkui.grass.DefaultGrassResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
public class ClientInvokerTest {

    private Method getUserName = MockClient.class.getMethod("getUseName", Long.class);

    private InvokerAttributes attributes;

    MockRegistry registry;

    List<ApplicationInstance> applicationInstanceList;

    MockProtocolClient client;

    List<Router> routerList;

    @BeforeEach
    public void init() {

        this.attributes = ServiceUtils.getInvokerAttributes(null, getUserName);

        applicationInstanceList = new ArrayList<>();
        applicationInstanceList.add(new MockDefaultApplicationInstance("test", "app", MockProtocolFactory.ID));

        routerList = new ArrayList<>();
        routerList.add(new MockRouter(applicationInstanceList));

        registry = new MockRegistry();
        registry.setApplicationInstanceList(applicationInstanceList);


        DefaultGrassResponse response = new DefaultGrassResponse();
        response.setBody("Success");

        client = new MockProtocolClient(CompletableFuture.completedFuture(response));
    }

    public ClientInvokerTest() throws NoSuchMethodException {
    }

    @Test
    public void testInitRequest(@Mock GrassContext context) throws IOException {
        ResetGrassContextManager.setContext(context);
        ClientInvoker invoker = new ClientInvoker("spi", "app", void.class, void.class, attributes);
        GrassRequest request = invoker.initRequest(new Object[] {new Long(1)});
        assertAll(
                () -> assertEquals("spi", request.getSpi()),
                () -> assertEquals("app", request.getApplicationName()),
                () -> assertTrue(request.getRequestTime() > 0),
                () -> assertEquals(1L, (Long)request.getBody().get("id"))
        );
    }

    @Test
    public void testCreateClientExecutor(@Mock GrassContext context) {
        when(context.getClientExecutorBuilder()).thenReturn(new DefaultClientExecutorBuilder(context, null));
        ResetGrassContextManager.setContext(context);

        ClientInvoker invoker = new ClientInvoker(null, "app", void.class, void.class, attributes);
        Executor executor = invoker.createClientExecutor();
        assertNotNull(executor);
    }

    @Test
    public void testInvoke(@Mock GrassContext context) throws Throwable {
        when(context.getProtocol("message")).thenReturn(client);
        when(context.getClientExecutorBuilder()).thenReturn(new DefaultClientExecutorBuilder(context, registry));
        when(context.getRouterList()).thenReturn(routerList);
        when(context.getDefaultLoadBalance()).thenReturn(new MockLoadBalance(0));

        ClientInvoker invoker = new ClientInvoker(null, "app", void.class, void.class, attributes);
        ResetGrassContextManager.setContext(context);
        String result = (String) invoker.invoke(new Object[] {new Long(1)});
        assertEquals("Success", result);
    }

}
