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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanglinkui.grass.ParameterAttribute;
import com.yanglinkui.grass.serialize.Context;
import com.yanglinkui.grass.serialize.DeserializeProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RequestBodyDeserializeProcessor implements DeserializeProcessor {

    @Override
    public boolean isSupport(Object description) {
        return description instanceof ParameterAttribute[];
    }

    @Override
    public <T> T deserialize(Context context, byte[] bytes, Object description) throws IOException, ClassNotFoundException {
        ObjectMapper objectMapper = context.getObject();
        JsonNode mapNode = objectMapper.readTree(bytes);
        return toMap((ParameterAttribute[]) description, objectMapper, mapNode);
    }

    private <T> T toMap(ParameterAttribute[] description, ObjectMapper objectMapper, JsonNode mapNode) throws IOException {
        Map<String, Object> parameterMap = new HashMap<>();

        ParameterAttribute[] attributes = description;
        for (ParameterAttribute attribute : attributes) {
            Object value = null;

            JsonNode node = mapNode.findValue(attribute.getName());
            if (node != null) {
                JavaType type = objectMapper.constructType(attribute.getType());
                value = objectMapper.readerFor(type).readValue(node);
                parameterMap.put(attribute.getName(), value);
            }

        }

        return (T)parameterMap;
    }

    @Override
    public <T> T deserialize(Context context, InputStream in, Object description) throws IOException, ClassNotFoundException {
        ObjectMapper objectMapper = context.getObject();
        JsonNode mapNode = objectMapper.readTree(in);
        return toMap((ParameterAttribute[]) description, objectMapper, mapNode);    }
}
