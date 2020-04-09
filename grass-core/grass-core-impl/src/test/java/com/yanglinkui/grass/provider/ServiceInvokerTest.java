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

import com.yanglinkui.grass.Invoker;
import com.yanglinkui.grass.InvokerAttributes;
import com.yanglinkui.grass.ParameterAttribute;
import com.yanglinkui.grass.mock.MockService;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ServiceInvokerTest {

    @Test
    public void testInvoke() throws Throwable {
        MockService service = new MockService(null, null , "test");
        Method defaultMethod = MockService.class.getMethod("defaultMethod", String.class);

        ServiceInvoker invoker = new ServiceInvoker(service, defaultMethod, null);
        assertEquals("test", invoker.invoke(new Object[]{"name"}));
    }

    @Test
    public void testEquals() throws Throwable {
        MockService service = new MockService(null, null , "test");
        Method defaultMethod = MockService.class.getMethod("defaultMethod", String.class);

        InvokerAttributes attributes1 = new InvokerAttributes("service.defaultMethod","", "", 0, new ParameterAttribute[0], String.class, String.class);
        InvokerAttributes attributes2 = new InvokerAttributes("service.defaultMethod","", "", 0, new ParameterAttribute[0], String.class, String.class);
        ServiceInvoker invoker1 = new ServiceInvoker(service, defaultMethod, attributes1);
        ServiceInvoker invoker2 = new ServiceInvoker(service, defaultMethod, attributes2);
        assertEquals(invoker1, invoker2);

        List<Invoker> invokerList = new ArrayList<>();
        invokerList.add(invoker1);
        assertTrue(invokerList.contains(invoker2));
    }
}
