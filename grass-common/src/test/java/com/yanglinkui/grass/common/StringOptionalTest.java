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

import org.junit.jupiter.api.Test;

import java.util.Optional;

public class StringOptionalTest {

    @Test
    public void testOf() {
        String val = StringOptional.of("a").orElse("b");
        assertEquals("a", val);
    }

    @Test
    public void testOf_null() {
        String val = StringOptional.of(null).orElse("b");
        assertEquals("b", val);
    }

    @Test
    public void testOf_empty() {
        String val = StringOptional.of("").orElse("b");
        assertEquals("b", val);
    }

    @Test
    public void testOf_supplier() {
        String val = StringOptional.of("").orElse(() -> "b");
        assertEquals("b", val);
    }

    @Test
    public void testOrElseThrow_supplier() {
        assertThrows(NullPointerException.class, () -> StringOptional.of(null).orElseThrow(() -> new NullPointerException()));
    }

    @Test
    public void testOrElseThrow_no_exception() throws Throwable {
        String val = StringOptional.of("a").orElseThrow(() -> new NullPointerException());
        assertEquals("a", val);
    }

}
