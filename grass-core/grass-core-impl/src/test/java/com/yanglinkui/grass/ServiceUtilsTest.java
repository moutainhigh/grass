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

import com.yanglinkui.grass.annotation.GrassMethod;
import com.yanglinkui.grass.annotation.GrassParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ServiceUtilsTest {

    private Method getData;

    private Method setData;

    private Method findByIdOrName;

    private Method getName;

    @BeforeEach
    public void init() throws NoSuchMethodException {
        this.getData = A.class.getMethod("getData");
        this.setData = A.class.getMethod("setData", String.class);
        this.findByIdOrName = A.class.getMethod("findByIdOrName", Long.class, String.class, Map.class);
        this.getName = A.class.getMethod("getName", String.class);
    }

    @Test
    public void testGetInvokerId() {
        String getDataServiceId = ServiceUtils.getInvokerId(null, getData.getAnnotation(GrassMethod.class), getData.getName());
        assertEquals("test", getDataServiceId);

        String setDataServiceId= ServiceUtils.getInvokerId(null, setData.getAnnotation(GrassMethod.class), setData.getName());
        assertEquals("setData", setDataServiceId);

        String findByIdOrNameServiceId = ServiceUtils.getInvokerId(null, findByIdOrName.getAnnotation(GrassMethod.class), findByIdOrName.getName());
        assertEquals("id", findByIdOrNameServiceId);

        getDataServiceId = ServiceUtils.getInvokerId("prefix", getData.getAnnotation(GrassMethod.class), getData.getName());
        assertEquals("prefix.test", getDataServiceId);

        getDataServiceId = ServiceUtils.getInvokerId("", getData.getAnnotation(GrassMethod.class), getData.getName());
        assertEquals("test", getDataServiceId);
    }

    @Test
    public void testGetParameterAttributes() {
        ParameterAttribute[] getDataDescriptions = ServiceUtils.getParameterAttributes(getData);
        assertEquals(0, getDataDescriptions.length);

        ParameterAttribute[] setDataDescriptions = ServiceUtils.getParameterAttributes(setData);
        assertAll(
                () -> assertEquals(1, setDataDescriptions.length),
                () -> assertEquals("_0", setDataDescriptions[0].getName()),
                () -> assertEquals(0, setDataDescriptions[0].getIndex()),
                () -> assertEquals(String.class, setDataDescriptions[0].getType())

        );

        ParameterAttribute[] findByIdOrNameDescriptions = ServiceUtils.getParameterAttributes(findByIdOrName);
        assertAll(
                () -> assertEquals(3, findByIdOrNameDescriptions.length),
                () -> assertEquals("id", findByIdOrNameDescriptions[0].getName()),
                () -> assertEquals(0, findByIdOrNameDescriptions[0].getIndex()),
                () -> assertEquals(Long.class, findByIdOrNameDescriptions[0].getType()),

                () -> assertEquals("name", findByIdOrNameDescriptions[1].getName()),
                () -> assertEquals(1, findByIdOrNameDescriptions[1].getIndex()),
                () -> assertEquals(String.class, findByIdOrNameDescriptions[1].getType()),

                () -> assertEquals("_2", findByIdOrNameDescriptions[2].getName()),
                () -> assertEquals(2, findByIdOrNameDescriptions[2].getIndex()),
                () -> assertEquals(Map.class, findByIdOrNameDescriptions[2].getType())

        );

        assertThrows(IllegalStateException.class, () -> ServiceUtils.getParameterAttributes(getName));

    }

    @Test
    public void testGetInvokerAttributes() {
        InvokerAttributes getDataAttributes = ServiceUtils.getInvokerAttributes("getData", this.getData);
        assertAll(
                () -> assertEquals("getData.test", getDataAttributes.getId()),
                () -> assertEquals("get", getDataAttributes.getAction()),
                () -> assertEquals("1.0", getDataAttributes.getVersion()),
                () -> assertEquals(Date.class, getDataAttributes.getReturnType()),
                () -> assertEquals(0, getDataAttributes.getParameterAttributes().length)
        );

        InvokerAttributes findByIdOrNameAttributes = ServiceUtils.getInvokerAttributes(null, this.findByIdOrName);

        assertEquals(3, findByIdOrNameAttributes.getParameterAttributes().length);

    }

    public static interface A {

        @GrassMethod(domain = "test", action = "get", version = "1.0", description = "get data")
        public Date getData();

        @GrassMethod
        public void setData(String a);

        @GrassMethod(id = "id", domain = "domain")
        public List<String> findByIdOrName(@GrassParam("id") Long id, @GrassParam("name") String name, Map<String, String> date);

        @GrassMethod
        public void getName(@GrassParam("_a") String a);
    }
}
