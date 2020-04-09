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
import com.yanglinkui.grass.mock.MockProcessorChain;
import com.yanglinkui.grass.mock.MockSpiRouter;
import com.yanglinkui.grass.exception.InvokedException;
import com.yanglinkui.grass.mock.MockRegistry;
import static org.junit.jupiter.api.Assertions.*;

import com.yanglinkui.grass.DefaultGrassResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SpiProcessorTest {

    MockRegistry registry;

    String spi = "testSpi";

    List<String> applicationList;

    @BeforeEach
    public void init() {
        applicationList = new ArrayList<>();
        applicationList.add("test1");
        applicationList.add("test2");
        applicationList.add("test3");
        applicationList.add("test4");

        registry = new MockRegistry();
        registry.setApplicationList(applicationList);
    }

    @Test
    public void testGetApplication() {
        MockSpiRouter spiRouter = new MockSpiRouter("test3");

        SpiProcessor processor = new SpiProcessor(spiRouter, registry);
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setSpi(spi);

        assertEquals("test3", processor.getApplication(request));
    }

    @Test
    public void testGetApplication_no_spiRouter() {
        SpiProcessor processor = new SpiProcessor(null, registry);
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setSpi(spi);

        InvokedException ie = assertThrows(InvokedException.class, () -> processor.getApplication(request));
        assertEquals("Not found SpiRouter for the spi(" + request.getSpi() + ")", ie.getMessage());

    }

    @Test
    public void testGetApplication_no_application() {
        MockSpiRouter spiRouter = new MockSpiRouter(null);

        SpiProcessor processor = new SpiProcessor(spiRouter, registry);
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setSpi(spi);

        InvokedException ie = assertThrows(InvokedException.class, () -> processor.getApplication(request));
        assertEquals("Not found application implementation of the spi(" + request.getSpi() + "), please check the spi spiRouter", ie.getMessage());
    }

    @Test
    public void testChoose() {
        MockSpiRouter spiRouter = new MockSpiRouter("application1");

        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setSpi(spi);

        MockProcessorChain<CompletableFuture<GrassResponse>> chain = new MockProcessorChain<>(request, (r) -> {
            assertEquals("application1", r.getApplicationName());
            return null;
        });

        SpiProcessor processor = new SpiProcessor(spiRouter, registry);
        processor.process(chain);


        //如果已经有了applicationName就不会被改变
        chain = new MockProcessorChain<>(request, CompletableFuture.completedFuture(new DefaultGrassResponse()));
        request.setApplicationName("application2");
        processor.process(chain);
        assertEquals("application2", request.getApplicationName());
    }

}
