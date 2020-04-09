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

package com.yanglinkui.grass.serialize;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@ExtendWith(MockitoExtension.class)
public class AbstractSerializerFactoryTest {

    @Test
    public void testRegister(@Mock SerializeProcessor serializeProcessor, @Mock DeserializeProcessor deserializeProcessor) {
        MySerializerFactory factory = new MySerializerFactory();
        assertEquals(0, factory.serializeProcessorList.size());
        factory.register(serializeProcessor);
        assertEquals(1, factory.serializeProcessorList.size());

        assertEquals(0, factory.deserializeProcessorList.size());
        factory.register(deserializeProcessor);
        assertEquals(1, factory.deserializeProcessorList.size());

    }

    private static class MySerializerFactory extends AbstractSerializerFactory {

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public Serializer getSerializer() {
            return new MySerializer();
        }
    }

    private static class MySerializer implements Serializer {


        @Override
        public byte[] serialize(Object value) throws IOException {
            return new byte[0];
        }

        @Override
        public void serialize(Object value, OutputStream out) throws IOException {

        }

        @Override
        public <T> T deserialize(byte[] bytes, Object description) throws IOException, ClassNotFoundException {
            return null;
        }

        @Override
        public <T> T deserialize(InputStream in, Object description) throws IOException, ClassNotFoundException {
            return null;
        }
    }

}
