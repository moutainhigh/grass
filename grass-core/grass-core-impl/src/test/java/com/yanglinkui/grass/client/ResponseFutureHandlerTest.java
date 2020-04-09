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

import com.yanglinkui.grass.Error;
import com.yanglinkui.grass.RawInputStream;
import com.yanglinkui.grass.exception.InvokedException;
import com.yanglinkui.grass.DefaultGrassResponse;
import com.yanglinkui.grass.mock.MockRawInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class ResponseFutureHandlerTest {

    private ResponseFutureHandler<String> handler;

    @BeforeEach
    public void init() {
        handler = new ResponseFutureHandler<>(String.class);
    }

    @Test
    public void testHandle_success() {
        DefaultGrassResponse response = new DefaultGrassResponse();
        response.setBody("Success");
        assertEquals("Success", handler.handle(response, null));

        response = new DefaultGrassResponse() {

            public RawInputStream getInputStream() {
                return new MockRawInputStream("Success1");
            }
        };

        assertEquals("Success1", handler.handle(response, null));
    }

    @Test
    public void testHandle_null_result() {
        DefaultGrassResponse response = new DefaultGrassResponse();
        assertEquals(null, handler.handle(response, null));
    }

    @Test
    public void testHandle_error() throws IOException {
        Error error = new Error(678, "Fail");

        DefaultGrassResponse response = new DefaultGrassResponse();
        response.setBody(error);
        response.setStatus(503);

        InvokedException ie = assertThrows(InvokedException.class, () -> handler.handle(response, null));
        assertEquals(678, ie.getCode());
        assertEquals("Fail", ie.getMessage());
    }

}
