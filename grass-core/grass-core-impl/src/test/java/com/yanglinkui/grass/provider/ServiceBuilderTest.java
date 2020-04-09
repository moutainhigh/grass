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

import com.yanglinkui.grass.InvokerAttributes;
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.mock.MockService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServiceBuilderTest {

    @Test
    public void testBuild() {
        MockService ms = new MockService(null, null, "aaa");

        ServiceBuilder builder = new ServiceBuilder();
        builder.setHandler(ms);
        Service<MockService> service = builder.build();

        assertEquals(ms, service.getObject());
        assertEquals(3, service.getInvokerList().size());

        service.getInvokerList().stream().forEach(invoker -> {
            InvokerAttributes attributes = invoker.getAttributes();
            if (attributes.getId().equals("service.users")) {
                assertEquals("list", attributes.getAction());
                assertEquals(List.class, attributes.getReturnTypeClass());
                assertEquals(1, attributes.getParameterAttributes().length);
                assertEquals(5_000, attributes.getTimeout());
                assertTrue(Utils.isEmpty(attributes.getVersion()));
            } else if (attributes.getId().equals("service.users-age")) {
                assertEquals("get", attributes.getAction());
                assertEquals(CompletableFuture.class, attributes.getReturnTypeClass());
                assertEquals(2, attributes.getParameterAttributes().length);
                assertEquals(0, attributes.getTimeout());
                assertEquals("1.0", attributes.getVersion());
            } else if (attributes.getId().equals("service.defaultMethod")) {
                assertTrue(Utils.isEmpty(attributes.getAction()));
                assertEquals(String.class, attributes.getReturnTypeClass());
                assertEquals(1, attributes.getParameterAttributes().length);
                assertEquals(0, attributes.getTimeout());
                assertTrue(Utils.isEmpty(attributes.getVersion()));
            } else {
                fail("unknow invoker");
            }
        });
    }
}
