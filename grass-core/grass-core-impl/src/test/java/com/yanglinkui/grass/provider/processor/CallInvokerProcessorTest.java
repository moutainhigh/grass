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

package com.yanglinkui.grass.provider.processor;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.mock.MockProcessorChain;
import com.yanglinkui.grass.mock.MockRawInputStream;
import com.yanglinkui.grass.mock.MockService;
import com.yanglinkui.grass.provider.Service;
import com.yanglinkui.grass.provider.ServiceBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CallInvokerProcessorTest {

    protected DefaultGrassRequest request;

    @BeforeEach
    public void init() {
        MockService ms = new MockService(Collections.EMPTY_LIST, CompletableFuture.completedFuture(7), "default");
        ServiceBuilder<MockService> builder = new ServiceBuilder<>();
        builder.setHandler(ms);
        Service service = builder.build();

        request = new DefaultGrassRequest() {
            @Override
            public RawInputStream getInputStream() {
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("id", new Long(1));
                objectMap.put("_1", "jonas");
                return new MockRawInputStream(objectMap);
            }
        };

        Optional<Invoker> optional = service.getInvokerList().stream().filter(i -> i.getAttributes().getId().equals("service.users-age")).findFirst();
        request.setAttribute("GRASS-INVOKER", optional.get());
        request.setAttribute("GRASS-INVOKER-ARGS", new Object[] {1L, "jonas"});

    }

    @Test
    public void testProcess() throws ExecutionException, InterruptedException {
        MockProcessorChain<GrassResponse> chain = new MockProcessorChain(request, new DefaultGrassResponse());

        CallInvokerProcessor processor = new CallInvokerProcessor();
        GrassResponse response = processor.process(chain);
        CompletableFuture<Integer> result = response.getBody();
        Assertions.assertEquals(7, result.get());
    }
}
