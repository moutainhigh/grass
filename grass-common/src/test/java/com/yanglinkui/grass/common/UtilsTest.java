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

package com.yanglinkui.grass.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

public class UtilsTest {

    @Test
    public void testCheckNotNull() {
        String value = Utils.checkNotNull("hello", "fail");
        Assertions.assertEquals(value, "hello");

        try {
            Utils.checkNotNull(null, "It %s is null.", "param");
        } catch(NullPointerException e) {
            Assertions.assertEquals("It param is null.", e.getMessage());
        }
    }

    @Test
    public void testIsDefault() throws NoSuchMethodException {
        Method hello = Example.class.getMethod("hello");
        assertTrue(Utils.isDefaultMethod(hello));

        Method hi = Example.class.getMethod("hi");
        Assertions.assertTrue(Utils.isDefaultMethod(hi) == false);
    }

    @Test
    public void testGetValue() {
        String value = Utils.getOrDefault("1", "2");
        Assertions.assertEquals(value, "1");

        assertEquals("2", Utils.getOrDefault("", "2"));
        assertEquals("2", Utils.getOrDefault(null, "2"));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(Utils.isEmpty(""));
        assertTrue(Utils.isEmpty(null));
        assertFalse(Utils.isEmpty("1"));
    }

    @Test
    public void isNotEmpty() {
        assertFalse(Utils.isNotEmpty(""));
        assertFalse(Utils.isNotEmpty(null));
        assertTrue(Utils.isNotEmpty("1"));
    }


    public static interface Example<T> {

        default public String hello() {
            return "hello";
        }

        public String hi();

        public Map<String, Example> say(String person);
    }
}
