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

package com.yanglinkui.grass.serialize.json;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.yanglinkui.grass.ParameterAttribute;
import com.yanglinkui.grass.serialize.Serializer;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JsonSerializerTest {

    private Serializer serializer;

    private Map<String, User> userList;

    private User user;

    private Type returnType;

    private ParameterAttribute[] attributes;

    @BeforeEach
    public void init() throws NoSuchMethodException {
        this.serializer = new JsonSerializerFactory().getSerializer();

        user = new User("jonas", true, 18);

        this.userList = new HashMap<>();
        userList.put("jonas", user);
        userList.put("robert", new User("robert", true, 7));

        returnType = this.getClass().getMethod("getUserList").getGenericReturnType();

        attributes = new ParameterAttribute[3];
        attributes[0] = new ParameterAttribute(User.class, "jonas", 0);
        attributes[1] = new ParameterAttribute(Integer.class, null, 1);
        attributes[2] = new ParameterAttribute(Boolean.class, "isNice", 2);
    }

    @Test
    public void testSerialize() throws IOException {
        String value = "jonas";
        byte[] bytes = this.serializer.serialize(value);
        assertEquals("\"jonas\"", new String(bytes));

        bytes = this.serializer.serialize(null);
        assertEquals("null", new String(bytes));

        bytes = this.serializer.serialize(user);
        assertEquals("{\"name\":\"jonas\",\"isBoy\":true,\"age\":18,\"boy\":true}", new String(bytes));

        bytes = this.serializer.serialize(userList);
        assertTrue(new String(bytes).indexOf("\"robert\":") != -1);
        assertTrue(new String(bytes).indexOf("\"jonas\":") != -1);
    }

    @Test
    public void testDeserialize() throws IOException, ClassNotFoundException {
        assertThrows(IllegalArgumentException.class, () -> this.serializer.deserialize("a".getBytes(), null));

        String value = "\"jonas\"";
        String v1 = this.serializer.deserialize(value.getBytes(), String.class);
        assertEquals("jonas", v1);

        assertNull(this.serializer.deserialize("null".getBytes(), String.class));
        assertNull(this.serializer.deserialize((byte[]) null, String.class));
        assertNull(this.serializer.deserialize((byte[]) null, null));


        value = "{\"name\":\"jonas\",\"isBoy\":true,\"age\":18,\"boy\":true}";
        User user = this.serializer.deserialize(value.getBytes(), User.class);
        assertEquals("jonas", user.getName());
        assertEquals(true, user.isBoy());
        assertEquals(18, user.getAge());

        value = "{\"robert\":{\"name\":\"robert\",\"isBoy\":true,\"age\":7,\"boy\":true},\"jonas\":{\"name\":\"jonas\",\"isBoy\":true,\"age\":18,\"boy\":true}}";
        Map<String, User> userList = this.serializer.deserialize(value.getBytes(), this.returnType);
        assertEquals(2, userList.size());
        assertEquals(User.class, userList.get("jonas").getClass());
        assertEquals("jonas", userList.get("jonas").getName());

        assertEquals(User.class, userList.get("robert").getClass());
        assertEquals("robert", userList.get("robert").getName());

    }

    @Test
    public void testSerialize_outputStream() throws IOException {
        String value = "jonas";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.serializer.serialize(value, out);
        assertEquals("\"jonas\"", new String(out.toByteArray()));

        assertThrows(IllegalArgumentException.class, () -> this.serializer.serialize(value, null));
    }

    @Test
    public void testDeserialize_inputStream() throws IOException, ClassNotFoundException {
        String value = "\"jonas\"";
        String v1 = this.serializer.deserialize(new ByteArrayInputStream(value.getBytes()), String.class);
        assertEquals("jonas", v1);

        assertNull(this.serializer.deserialize((InputStream) null, String.class));
    }

    @Test
    public void testRequestBodyProcessor() throws IOException, ClassNotFoundException {
        Map<String, Object> parameterList = new HashMap<>();
        parameterList.put("jonas", user);
        parameterList.put("isNice", Boolean.TRUE);
        parameterList.put("_1", 200);

        byte[] value = this.serializer.serialize(parameterList);

        Map<String, Object> result = this.serializer.deserialize(value, attributes);
        assertEquals(User.class, result.get("jonas").getClass());


        assertEquals(Integer.class, result.get("_1").getClass());
        assertEquals(Boolean.class, result.get("isNice").getClass());
    }

    public Map<String, User> getUserList() {
        return null;
    }

}
