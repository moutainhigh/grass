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

package com.yanglinkui.grass.protocol.message.api;

import java.util.Map;

public class Message {

    private Type type = Type.REQUEST;

    private String id;

    private String correlationId;

    private String contentType;

    private String contentEncoding;

    private byte[] body;

    private Map<String, String> properties;

    public Message(byte[] body) {
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Message setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public byte[] getBody() {
        return body;
    }

    public Message setBody(byte[] body) {
        this.body = body;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public Message setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public Message setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public Message setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        REQUEST, EVENT
    }
}
