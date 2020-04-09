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

import com.yanglinkui.grass.GrassResponse;
import com.yanglinkui.grass.DefaultGrassRequest;
import com.yanglinkui.grass.DefaultGrassResponse;
import com.yanglinkui.grass.exception.InvokedException;
import com.yanglinkui.grass.mock.MockProcessorChain;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CheckRequestProcessorTest {

    @Test
    public void testProcess_different_zone() {
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setZone("tset");
        request.setApplicationName("app1");

        DefaultGrassResponse response = new DefaultGrassResponse();
        response.setBody("Success");
        MockProcessorChain<GrassResponse> chain = new MockProcessorChain<>(request, response);

        CheckRequestProcessor processor = new CheckRequestProcessor("test", "app1");
        InvokedException e = assertThrows(InvokedException.class, () -> processor.process(chain));
        assertEquals("The requested zone(tset of app1) is different from test", e.getMessage());
    }

    @Test
    public void testProcess_different_applicationName() {
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setZone("test");
        request.setApplicationName("app2");

        DefaultGrassResponse response = new DefaultGrassResponse();
        response.setBody("Success");
        MockProcessorChain<GrassResponse> chain = new MockProcessorChain<>(request, response);

        CheckRequestProcessor processor = new CheckRequestProcessor("test", "app1");
        InvokedException e = assertThrows(InvokedException.class, () -> processor.process(chain));
        assertEquals("The requested application(app2) is different from app1", e.getMessage());
    }

    @Test
    public void testProcess_no_serviceId() {
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setId("1");
        request.setZone("test");
        request.setApplicationName("app1");

        DefaultGrassResponse response = new DefaultGrassResponse();
        response.setBody("Success");
        MockProcessorChain<GrassResponse> chain = new MockProcessorChain<>(request, response);

        CheckRequestProcessor processor = new CheckRequestProcessor("test", "app1");
        InvokedException e = assertThrows(InvokedException.class, () -> processor.process(chain));
        assertEquals("No service Id of request: 1", e.getMessage());
    }

    @Test
    public void testProcess() {
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setId("1");
        request.setZone("test");
        request.setApplicationName("app1");
        request.setServiceId("users");

        DefaultGrassResponse response = new DefaultGrassResponse();
        response.setBody("Success");
        MockProcessorChain<GrassResponse> chain = new MockProcessorChain<>(request, response);

        CheckRequestProcessor processor = new CheckRequestProcessor("test", "app1");
        GrassResponse response1 = processor.process(chain);
        assertEquals("Success", response1.getBody());
    }
}
