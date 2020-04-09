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

public class GrassResponseWrapper implements GrassResponse {

    private final GrassResponse response;

    public GrassResponseWrapper(GrassResponse response) {
        this.response = response;
    }


    @Override
    public String getCorrelationId() {
        return this.response.getCorrelationId();
    }

    @Override
    public String getContentType() {
        return this.response.getContentType();
    }

    @Override
    public String getAction() {
        return this.response.getAction();
    }

    @Override
    public String getVersion() {
        return this.response.getVersion();
    }

    @Override
    public String getApplicationName() {
        return this.response.getApplicationName();
    }

    @Override
    public String getInstanceId() {
        return this.response.getInstanceId();
    }

    @Override
    public String getZone() {
        return this.response.getZone();
    }

    @Override
    public String getServiceId() {
        return this.response.getServiceId();
    }

    @Override
    public String[] getHeaderNames() {
        return this.response.getHeaderNames();
    }

    @Override
    public String getHeader(String key) {
        return this.response.getHeader(key);
    }

    @Override
    public void setHeader(String key, String value) {
        this.response.setHeader(key, value);
    }

    @Override
    public void removeHeader(String key) {
        this.response.removeHeader(key);
    }

    @Override
    public int getStatus() {
        return this.response.getStatus();
    }

    @Override
    public <T> T getBody() {
        return this.response.getBody();
    }

    @Override
    public Long getInvokedTime() {
        return this.response.getInvokedTime();
    }

    @Override
    public Long getReturnedTime() {
        return this.response.getReturnedTime();
    }

    @Override
    public RawInputStream getInputStream() {
        return this.response.getInputStream();
    }
}
