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

import com.yanglinkui.grass.Invoker;
import com.yanglinkui.grass.InvokerAttributes;
import com.yanglinkui.grass.mock.MockClient;
import static org.junit.jupiter.api.Assertions.*;

import com.yanglinkui.grass.common.Utils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientBuilderTest {

    @Test
    public void testBuild() {
        ClientBuilder<MockClient> builder = new ClientBuilder<>();
        builder.setInterface(MockClient.class);
        MockClient client = builder.build();

        assertNotNull(client);

        List<Invoker> list = ((Client) client).getInvokerList();
        assertEquals(3, list.size());

        list.stream().forEach(invoker -> {
            InvokerAttributes attributes = invoker.getAttributes();
            if (attributes.getId().equals("users")) {
                assertEquals("list", attributes.getAction());
                assertEquals(List.class, attributes.getReturnTypeClass());
                assertEquals(1, attributes.getParameterAttributes().length);
                assertEquals(5_000, attributes.getTimeout());
                assertTrue(Utils.isEmpty(attributes.getVersion()));
            } else if (attributes.getId().equals("users-age")) {
                assertEquals("get", attributes.getAction());
                assertEquals(CompletableFuture.class, attributes.getReturnTypeClass());
                assertEquals(2, attributes.getParameterAttributes().length);
                assertEquals(0, attributes.getTimeout());
                assertEquals("1.0", attributes.getVersion());
            } else if (attributes.getId().equals("defaultMethod")) {
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
