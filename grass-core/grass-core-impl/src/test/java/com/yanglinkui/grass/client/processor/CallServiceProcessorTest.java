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
import com.yanglinkui.grass.mock.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
public class CallServiceProcessorTest {

    Processor.Chain<CompletableFuture<GrassResponse>> chain;

    private MockProtocolClient client;

    private ApplicationInstance applicationInstance;

    private GrassRequest request;

    private CompletableFuture<GrassResponse> completableFuture = new CompletableFuture<>();

    @BeforeEach
    public void init() {
        applicationInstance = new MockDefaultApplicationInstance("app1");
        request = new DefaultGrassRequest();
        request.setAttribute(ProcessorConstants.Request.Attribute.APPLICATION_INSTANCE, applicationInstance);

        client = new MockProtocolClient(completableFuture);
        chain = new MockProcessorChain<>(request, null);

    }

    @Test
    public void testProcess(@Mock GrassContext context) {
        when(context.getProtocol("message")).thenReturn(client);

        CallServiceProcessor processor = new CallServiceProcessor(context, 3_000);
        CompletableFuture<GrassResponse> result = processor.process(chain);
        Assertions.assertEquals(completableFuture, result);
    }
}
