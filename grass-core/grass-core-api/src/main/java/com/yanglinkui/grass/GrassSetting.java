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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GrassSetting {

    private String zone;

    private String applicationName;

    private String spi;

    private String instanceId;

    private String protocol;

    private Map<String, String> metadataList = new HashMap<>();

    public GrassSetting(String applicationName) {
        this.applicationName = applicationName;
    }

    public GrassSetting(String zone, String applicationName, String instanceId) {
        this.zone = zone;
        this.applicationName = applicationName;
        this.instanceId = instanceId;
    }

    public String getZone() {
        return zone;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getSpi() {
        return spi;
    }

    public void setSpi(String spi) {
        this.spi = spi;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getMetadata(String key) {
        return this.metadataList.get(key);
    }

    public void addMetadata(String key, String value) {
        this.metadataList.put(key, value);
    }

    public Map<String, String> getMetadataList() {
        return Collections.unmodifiableMap(this.metadataList);
    }



}
