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

import java.util.HashMap;
import java.util.Map;

/**
 * 非线性安全，保证在一个线程内使用
 */
public class DefaultGrassResponse implements GrassResponse {

    private int status = GrassResponse.SUCCESS;

    private String correlationId;

    private String contentType;

    private String applicationName;

    private String instanceId;

    private String zone;

    private String serviceId;

    private String action;

    private String version;

    private Object body;

    private Long invokedTime;

    private Long returnedTime;

    private Map<String, String> headers = new HashMap<>();

    private transient RawInputStream inputStream;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public <T> T getBody() {
        return (T) this.body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Long getInvokedTime() {
        return invokedTime;
    }

    public void setInvokedTime(Long invokedTime) {
        this.invokedTime = invokedTime;
    }

    public Long getReturnedTime() {
        return returnedTime;
    }

    public void setReturnedTime(Long returnedTime) {
        this.returnedTime = returnedTime;
    }

    public String[] getHeaderNames() {
        return this.headers.keySet().toArray(new String[0]);
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    @Override
    public void removeHeader(String key) {
        this.headers.remove(key);
    }

    @Override
    public RawInputStream getInputStream() {
        return this.inputStream;
    }

    public void setInputStream(RawInputStream inputStream) {
        this.inputStream = inputStream;
    }
}
