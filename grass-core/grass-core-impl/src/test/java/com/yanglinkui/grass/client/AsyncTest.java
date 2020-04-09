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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
public class AsyncTest {

    private Method getNameMethod =  AsyncClient.class.getMethod("getName");

    @Mock
    private AsyncClient client;

    public AsyncTest() throws NoSuchMethodException {
    }

    @BeforeEach
    public void init() {
        when(client.async(getNameMethod)).thenReturn(CompletableFuture.completedFuture("Jonas"));
    }

    @Test
    public void testAsync() throws NoSuchMethodException, ExecutionException, InterruptedException {
        Async async = new Async(client);
        Async.Method getName = async.of(getNameMethod);
        assertEquals("Jonas", getName.invoke().get());
    }

    public interface AsyncClient extends Client {

        public String getName();

    }
}
