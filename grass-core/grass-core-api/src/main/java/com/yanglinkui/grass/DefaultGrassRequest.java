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

public class DefaultGrassRequest implements GrassRequest {

    private String id;

    private String contentType;

    private String zone;

    private Integer zoneVersion;

    private Long requestTime;

    //service info

    private String serviceId;

    private String action;

    private String applicationName;

    private String spi;

    private String version;

    private Map<String, Object> parameterList = new HashMap<>();

    //remote info

    private String remoteUser;

    private Map<String, String> headers = new HashMap<>();

    private transient Map<String, Object> attributes = new HashMap<>();

    private transient RawInputStream inputStream;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getZone() {
        return zone;
    }

    public Integer getZoneVersion() {
        return zoneVersion;
    }

    public void setZoneVersion(Integer zoneVersion) {
        this.zoneVersion = zoneVersion;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Long requestTime) {
        this.requestTime = requestTime;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }


    public String getServiceVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRemoteUser() {
        return this.remoteUser;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    public String[] getHeaderNames() {
        return this.headers.keySet().toArray(new String[0]);
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    public void setHeader(String key, String value) {
        if (value == null) {
            return;
        }
        this.headers.put(key, value);
    }

    public void removeHeader(String key) {
        this.headers.remove(key);
    }

    public String getSpi() {
        return spi;
    }

    public void setSpi(String spi) {
        this.spi = spi;
    }

    public void setAttribute(String key, Object value) {
        if (value == null) {
            return;
        }
        this.attributes.put(key, value);
    }

    public <T> T getAttribute(String key) {
        return (T) this.attributes.get(key);
    }

    @Override
    public Map<String, Object> getBody() {
        return this.parameterList;
    }

    public void setBody(Map<String, Object> parameterList) {
        this.parameterList = parameterList;
    }

    @Override
    public RawInputStream getInputStream() {
        return this.inputStream;
    }

    public void setInputStream(RawInputStream inputStream) {
        this.inputStream = inputStream;
    }
}
