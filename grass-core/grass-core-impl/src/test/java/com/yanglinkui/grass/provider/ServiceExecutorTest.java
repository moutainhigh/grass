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
import com.yanglinkui.grass.mock.MockService;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@ExtendWith(MockitoExtension.class)
public class ServiceExecutorTest {

    private DefaultGrassRequest request;

    private AbstractRepository<Service> repository;

    @BeforeEach
    public void init() {
        MockService ms = new MockService();
        ServiceBuilder<MockService> builder = new ServiceBuilder();
        builder.setHandler(ms);

        repository = new DefaultServiceRepository();
        repository.add(builder.build());

        request = new DefaultGrassRequest();
        request.setZone("test");
        request.setApplicationName("app");
        request.setServiceId("service.users-age");
        request.setAction("get");
        request.setVersion("1.0");
    }

    @Test
    public void testExecute(@Mock GrassContext context) {
        List<ServiceProcessor> processorList = new ArrayList<>();
        final List<String> processorParameterList = new ArrayList<>();
        processorList.add(new TestProcessor((r) -> {
            processorParameterList.add(r.getServiceId());
            return null;
        }));

        when(context.getServiceProcessorList()).thenReturn(processorList);
        when(context.getZone()).thenReturn("test");
        when(context.getApplicationName()).thenReturn("app");

        ServiceExecutor executor = new ServiceExecutor(context, repository);
        GrassResponse response = executor.execute(request);
        assertEquals(GrassResponse.SUCCESS, response.getStatus());
        assertEquals(context.getApplicationName(), response.getApplicationName());
        assertEquals(context.getZone(), response.getZone());
        assertEquals(request.getServiceId(), response.getServiceId());
        assertEquals(request.getAction(), response.getAction());
        assertEquals(request.getServiceVersion(), response.getVersion());
        assertEquals("service.users-age", processorParameterList.get(0));

    }

    @Test
    public  void testExecute_error(@Mock GrassContext context) {
        when(context.getServiceProcessorList()).thenReturn(Collections.EMPTY_LIST);
        when(context.getZone()).thenReturn("test");
        when(context.getApplicationName()).thenReturn("app");

        request.setZone("tset");

        ServiceExecutor executor = new ServiceExecutor(context, repository);
        GrassResponse response = executor.execute(request);
        assertNotEquals(GrassResponse.SUCCESS, response.getStatus());
        assertEquals(context.getApplicationName(), response.getApplicationName());
        assertEquals(context.getZone(), response.getZone());
        assertEquals(null, response.getServiceId());
        assertEquals(null, response.getAction());
        assertEquals(null, response.getVersion());

        assertTrue(response.getBody() instanceof Error);
        assertEquals("The requested zone(tset of app) is different from test", ((Error) response.getBody()).getMessage());
    }

    private static class TestProcessor implements ServiceProcessor {

        private final Function<GrassRequest, String> callback;

        public TestProcessor(Function<GrassRequest, String> callback) {
            this.callback = callback;
        }

        @Override
        public GrassResponse process(Chain<GrassResponse> chain) {
            callback.apply(chain.getRequest());
            return chain.proceed(chain.getRequest());
        }
    }

}
