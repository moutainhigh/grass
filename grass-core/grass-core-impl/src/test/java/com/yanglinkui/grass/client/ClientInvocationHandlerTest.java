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

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.mock.MockClient;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
public class ClientInvocationHandlerTest {

    Method getUserAge;
    Method getUseName;
    InvokerAttributes getUserAgeAttributes;
    InvokerAttributes getUserNameAttributes;

    public ClientInvocationHandlerTest() throws NoSuchMethodException {
    }

    @BeforeEach
    public void init() throws NoSuchMethodException {
        getUserAge = MockClient.class.getMethod("getUserAge", Long.class, String.class);
        getUseName = MockClient.class.getMethod("getUseName", Long.class);

        getUserAgeAttributes = ServiceUtils.getInvokerAttributes(null, getUserAge);
        getUserNameAttributes = ServiceUtils.getInvokerAttributes(null, getUseName);
    }

    @Test
    public void testInvoke_hashCode() throws Throwable {
        ClientInvocationHandler handler = new ClientInvocationHandler("id", MockClient.class, new HashMap<>(), new HashMap<>(), void.class, void.class);

        Method hashCode = ClientInvocationHandler.class.getMethod("hashCode");
        assertTrue(handler.invoke(this, hashCode, new Object[0]) != null);
    }

    @Test
    public void testInvoke_equals() throws Throwable {
        ClientInvocationHandler handler = new ClientInvocationHandler("id", MockClient.class, new HashMap<>(), new HashMap<>(), void.class, void.class);

        Method equlas = ClientInvocationHandler.class.getMethod("equals", Object.class);
        assertTrue(handler.invoke(this, equlas, new Object[]{handler}) == Boolean.FALSE);
    }

    @Test
    public void testInvoke_toString() throws Throwable {
        ClientInvocationHandler handler = new ClientInvocationHandler("id", MockClient.class, new HashMap<>(), new HashMap<>(), void.class, void.class);

        Method toString = ClientInvocationHandler.class.getMethod("toString");
        assertTrue(handler.invoke(this, toString, new Object[0]) != null);
    }

    @Test
    public void testInvoke_async() throws Throwable {
        Map<Method, Invoker> dispatch = new HashMap<>();
        dispatch.put(getUserAge, new MockInvoker("spi", null, null, null, getUserAgeAttributes));

        Method async = Client.class.getMethod("async", Method.class, Object[].class);

        ClientInvocationHandler handler = new ClientInvocationHandler("id", MockClient.class, dispatch, new HashMap<>(), void.class, void.class);
        Object result = handler.invoke(this, async, new Object[]{getUserAge, 1L});
        assertEquals(CompletableFuture.class, result.getClass());
        assertEquals(7, ((CompletableFuture)result).get());
    }

    @Test
    public void testInvoke_getInvokerList(@Mock Invoker invoker) throws Throwable {
        Map<Method, Invoker> dispatch = new HashMap<>();
        dispatch.put(getUserAge, invoker);

        Method getInvokerList = Client.class.getMethod("getInvokerList");
        ClientInvocationHandler handler = new ClientInvocationHandler("id", MockClient.class, dispatch, new HashMap<>(), void.class, void.class);
        Object result = handler.invoke(this, getInvokerList, new Object[0]);
        assertNotNull(result);
        assertEquals(1, ((List)result).size());
        assertEquals(invoker, ((List)result).get(0));
    }

    @Test
    public void testInvoke(@Mock Invoker getUserAgeInvoker, @Mock Invoker exceptionInvoker) throws Throwable {
        Object[] args = new Object[]{1L};
        when(getUserAgeInvoker.invoke(args)).thenReturn(7);
        when(exceptionInvoker.invoke(args)).thenThrow(new NullPointerException());


        Map<Method, Invoker> dispatch = new HashMap<>();
        dispatch.put(getUserAge, getUserAgeInvoker);
        dispatch.put(getUseName, exceptionInvoker);

        ClientInvocationHandler handler = new ClientInvocationHandler("id", MockClient.class, dispatch, new HashMap<>(), void.class, void.class);
        assertEquals(7, handler.invoke(this, getUserAge, args));
        assertThrows(NullPointerException.class, () -> handler.invoke(this, getUseName, args));
    }

    @Test
    public void testInvoke_fallback(@Mock Invoker exceptionInvoker, @Mock FallbackFactory fallbackFactory, @Mock GrassContext context) throws Throwable {
        when(context.getInstance(MockClientTest.class)).thenReturn(new MockClientTest());
        when(context.getInstance(fallbackFactory.getClass())).thenReturn(fallbackFactory);

        Object[] args = new Object[]{1L};
        when(exceptionInvoker.invoke(args)).thenThrow(new NullPointerException("just test"));

        Map<Method, Invoker> dispatch = new HashMap<>();
        dispatch.put(getUseName, exceptionInvoker);

        ClientInvocationHandler handler = new ClientInvocationHandler("id", MockClient.class, dispatch, new HashMap<>(), MockClientTest.class, fallbackFactory.getClass());
        ResetGrassContextManager.setContext(context);
        List<String> nameList = (List<String>) handler.invoke(this, getUseName, args);
        assertEquals(2, nameList.size());
    }



    private static class MockInvoker extends ClientInvoker {

        public MockInvoker(String spi, String applicationName, Class<?> router, Class<?> loadBalance, InvokerAttributes invokerAttributes) {
            super(spi, applicationName, router, loadBalance, invokerAttributes);
        }

        @Override
        public Object invoke(Object[] args, boolean isAsync) throws Throwable {
            return CompletableFuture.completedFuture(7);
        }
    }

    private static class MockClientTest implements MockClient {
        @Override
        public List<String> getUseName(Long id) {
            List<String> nameList = new ArrayList<>();
            nameList.add("jonas");
            nameList.add("robert");
            return nameList;
        }

        @Override
        public CompletableFuture<Integer> getUserAge(Long id, String name) {
            return CompletableFuture.completedFuture(8);
        }
    }

}
