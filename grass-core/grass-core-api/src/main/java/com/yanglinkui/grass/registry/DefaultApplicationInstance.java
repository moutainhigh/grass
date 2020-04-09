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

package com.yanglinkui.grass.registry;

import com.yanglinkui.grass.ApplicationInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultApplicationInstance implements ApplicationInstance {

    private String id;

    private String applicationName;

    private String spi;

    private String instanceId;

    private String addresses;

    private boolean isSecure;

    private String protocol;

    private String zone;

    private Map<String, String> metadata = new HashMap<>();

    private long lastActiveTime;

    private Integer zoneVersion;

    private boolean isActive;

    public String getId() {
        return id;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getSpi() {
        return spi;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getAddresses() {
        return addresses;
    }

    public boolean isSecure() {
        return isSecure;
    }

    /**
     * 应付javabean工具
     * @return
     */
    protected boolean getSecure() {
        return isSecure;
    }

    public String getZone() {
        return zone;
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public String getMetadata(String name) {
        return this.metadata.get(name);
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public Integer getZoneVersion() {
        return zoneVersion;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setSpi(String spi) {
        this.spi = spi;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public void setZoneVersion(Integer zoneVersion) {
        this.zoneVersion = zoneVersion;
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * 应付javabean工具
     * @return
     */
    protected boolean getActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public int compareTo(ApplicationInstance that) {
        if (that == null) {
            return Integer.MAX_VALUE;
        }

        if (!ApplicationInstance.class.isAssignableFrom(that.getClass())) {
            return Integer.MAX_VALUE;
        }

        return that.getZoneVersion().compareTo(this.zoneVersion);
    }
}
