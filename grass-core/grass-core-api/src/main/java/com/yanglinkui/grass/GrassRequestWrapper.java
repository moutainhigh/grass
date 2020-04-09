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

import java.util.Map;

public class GrassRequestWrapper implements GrassRequest {

    private final GrassRequest request;

    public GrassRequestWrapper(GrassRequest request) {
        this.request = request;
    }

    @Override
    public String getId() {
        return this.request.getId();
    }

    @Override
    public String getContentType() {
        return this.request.getContentType();
    }

    @Override
    public String getZone() {
        return this.request.getZone();
    }

    @Override
    public Integer getZoneVersion() {
        return this.request.getZoneVersion();
    }

    @Override
    public Long getRequestTime() {
        return this.request.getRequestTime();
    }

    @Override
    public String getServiceId() {
        return this.request.getServiceId();
    }

    @Override
    public String getAction() {
        return this.request.getAction();
    }

    @Override
    public String getApplicationName() {
        return this.request.getApplicationName();
    }

    @Override
    public String getServiceVersion() {
        return this.request.getServiceVersion();
    }

    @Override
    public String getRemoteUser() {
        return this.request.getRemoteUser();
    }

    @Override
    public String[] getHeaderNames() {
        return this.request.getHeaderNames();
    }

    @Override
    public String getHeader(String key) {
        return this.request.getHeader(key);
    }

    @Override
    public void setHeader(String key, String value) {
        this.request.setHeader(key, value);
    }

    @Override
    public void removeHeader(String key) {
        this.request.removeHeader(key);
    }

    @Override
    public String getSpi() {
        return this.request.getSpi();
    }

    @Override
    public void setAttribute(String key, Object value) {
        this.request.setAttribute(key, value);
    }

    @Override
    public <T> T getAttribute(String key) {
        return this.request.getAttribute(key);
    }

    @Override
    public Map<String, Object> getBody() {
        return this.request.getBody();
    }

    @Override
    public RawInputStream getInputStream() {
        return this.request.getInputStream();
    }
}
