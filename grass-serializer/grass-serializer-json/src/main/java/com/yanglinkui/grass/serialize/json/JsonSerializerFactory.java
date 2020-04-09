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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanglinkui.grass.serialize.AbstractSerializerFactory;
import com.yanglinkui.grass.serialize.Serializer;

public class JsonSerializerFactory extends AbstractSerializerFactory {

    protected final ObjectMapper objectMapper;

    public JsonSerializerFactory() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.deserializeProcessorList.add(new RequestBodyDeserializeProcessor());
    }

    @Override
    public String getId() {
        return "json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public Serializer getSerializer() {
        return new JsonSerializer(this.objectMapper, this.serializeProcessorList, this.deserializeProcessorList);
    }

}
