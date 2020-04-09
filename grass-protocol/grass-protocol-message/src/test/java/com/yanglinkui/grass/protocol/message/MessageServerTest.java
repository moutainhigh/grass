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

package com.yanglinkui.grass.protocol.message;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.provider.Service;
import com.yanglinkui.grass.provider.ServiceExecutorBuilder;
import com.yanglinkui.grass.provider.ServiceRepository;
import com.yanglinkui.grass.serialize.Serializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServerTest {

    Logger logger = LoggerFactory.getLogger(MessageServerTest.class);

    @Test
    public void testStart(@Mock GrassContext context, @Mock ServiceRepository serviceRepository, @Mock ServiceExecutorBuilder serviceExecutorBuilder, @Mock Serializer serializer) throws InterruptedException, IOException {
        logger.error("000000");

        when(context.getZone()).thenReturn("test");
        when(context.getApplicationName()).thenReturn("app");
        when(context.getApplicationInstanceId()).thenReturn("app-1");
        when(context.getServiceExecutorBuilder()).thenReturn(serviceExecutorBuilder);
        when(context.getSerializer(any())).thenReturn(serializer);
        when(serializer.serialize(any())).thenReturn("success".getBytes());
        ResetGrassContextManager.setContext(context);

        when(serviceExecutorBuilder.setServiceRepository(any())).thenReturn(serviceExecutorBuilder);
        when(serviceExecutorBuilder.build()).thenReturn(new MockServiceExecutor("jonas"));

        InvokerAttributes attributes = new InvokerAttributes("test.user", "get", null, 0, new ParameterAttribute[0], String.class, String.class);
        MockInvoker invoker = new MockInvoker("success", attributes);
        List<Invoker> invokerList = new LinkedList<>();
        invokerList.add(invoker);
        MockService service = new MockService(new Object(), Service.Type.PUBLIC, invokerList);

        List<Service> serviceList = new LinkedList<>();
        serviceList.add(service);
        when(serviceRepository.getList()).thenReturn(serviceList);

        Map<String, String> config = new HashMap<>();
        config.put("rabbitmq.username", "guest");
        config.put("rabbitmq.password", "guest");
        config.put("rabbitmq.vhost", "/");
        MessageServer server = new MessageServer("rabbitmq", "test", "localhost:5672", config, Executors.newFixedThreadPool(20));
        server.exportPublic(serviceRepository);
        server.exportManagement(serviceRepository);
        server.start();
        synchronized (MessageServerTest.class) {
            MessageServerTest.class.wait();
        }

        server.shutdown();
    }
}
