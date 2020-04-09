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
import com.yanglinkui.grass.mock.MockService;
import com.yanglinkui.grass.provider.DefaultServiceRepository;
import com.yanglinkui.grass.provider.Service;
import com.yanglinkui.grass.provider.ServiceBuilder;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class FindServiceProcessorTest {

    @Test
    public void testProcess() {
        MockService ms = new MockService();
        ServiceBuilder<MockService> builder = new ServiceBuilder();
        builder.setHandler(ms);

        AbstractRepository<Service> repository = new DefaultServiceRepository();
        repository.add(builder.build());

        FindServiceProcessor processor = new FindServiceProcessor(repository);
        DefaultGrassRequest request = new DefaultGrassRequest();
        request.setServiceId("service.users-age");
        request.setAction("get");
        request.setVersion("1.0");

        MockProcessorChain chain = new MockProcessorChain(request, new DefaultGrassResponse());

        processor.process(chain);

        assertNotNull(request.getAttribute("GRASS-INVOKER"));
        assertEquals("service.users-age", ((Invoker) request.getAttribute("GRASS-INVOKER")).getAttributes().getId());

    }
}
