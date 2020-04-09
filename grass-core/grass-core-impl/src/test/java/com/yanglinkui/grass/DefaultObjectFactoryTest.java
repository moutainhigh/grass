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

package com.yanglinkui.grass;

import static org.junit.jupiter.api.Assertions.*;

import ch.qos.logback.core.spi.ContextAware;
import com.yanglinkui.grass.exception.NoUniqueException;
import com.yanglinkui.grass.mock.MockProtocolClient;
import com.yanglinkui.grass.mock.MockProtocolFactory;
import com.yanglinkui.grass.protocol.DefaultProtocolFactoryManager;
import com.yanglinkui.grass.protocol.ProtocolFactoryManager;
import com.yanglinkui.grass.serialize.DefaultSerializerFactoryManager;
import com.yanglinkui.grass.serialize.SerializerFactoryManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class DefaultObjectFactoryTest {

    private DefaultObjectFactory context;

    @BeforeEach
    public void init() {
        context = new DefaultObjectFactory(Collections.EMPTY_LIST);
    }

    @Test
    public void testAddInstance_object() {
        Object value = new Object();
        context.addInstance("test", value);
        assertEquals(value, context.getInstance("test"));
    }

    @Test
    public void testAddInstance_inheritedObjects() {
        context.addInstance("string", "jonas");
        assertEquals("jonas", context.getInstance("string"));

        MyObject myObject = new MyObject();
        context.addInstance("myObject", myObject);
        assertEquals(myObject, context.getInstance(MyObject.class));
        assertEquals(myObject, context.getInstance(Error.class));
        assertEquals(myObject, context.getInstance(ObjectFactory.class));
        assertEquals(myObject, context.getInstance(Order.class));
        assertEquals(myObject, context.getInstance(Invoker.class));
    }

    @Test
    public void testGetInstance_class() {
        context.addInstance("jonas", "jonas");
        assertEquals("jonas", context.getInstance(String.class));

        context.addInstance("robert", "robert");
        assertThrows(NoUniqueException.class, () -> context.getInstance(String.class));
    }

    @Test
    public void testGetInstance_class_id() {
        context.addInstance("jonas", "jonas");
        context.addInstance("robert", "robert");

        assertEquals("jonas", context.getInstance(String.class, "jonas"));
        assertEquals("robert", context.getInstance(String.class, "robert"));
    }

    public static class MyObject extends Error implements ObjectFactory, Order, Invoker {

        @Override
        public <T> T getInstance(Class<?> clazz, String id) {
            return null;
        }

        @Override
        public <T> T getInstance(Class<?> clazz) {
            return null;
        }

        @Override
        public <T> T getInstance(String id) {
            return null;
        }


        @Override
        public Object invoke(Object[] args) throws Throwable {
            return null;
        }

        @Override
        public InvokerAttributes getAttributes() {
            return null;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }
}
