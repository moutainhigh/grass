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

package com.yanglinkui.grass.mock;

import com.yanglinkui.grass.ApplicationInstance;
import com.yanglinkui.grass.GrassSetting;
import com.yanglinkui.grass.Zone;
import com.yanglinkui.grass.ZoneVersion;
import com.yanglinkui.grass.registry.Registry;
import com.yanglinkui.grass.registry.DefaultZone;
import com.yanglinkui.grass.registry.DefaultZoneVersion;

import java.util.List;

public class MockRegistry implements Registry {

    private List<ApplicationInstance> applicationInstanceList;

    private DefaultZone zone;

    private List<String> applicationList;

    @Override

    public String getId() {
        return "mock";
    }

    @Override
    public void register(GrassSetting setting) {
    }

    @Override
    public boolean ping() {
        return true;
    }

    @Override
    public Zone getZone(String name) {
        return this.zone;
    }

    @Override
    public ZoneVersion getZoneVersion(String zone, Integer zoneVersionId) {
        return null;
    }

    @Override
    public List<String> getApplicationList(String spi) {
        return this.applicationList;
    }

    @Override
    public List<ApplicationInstance> getApplicationInstanceList(String zone, String applicationName) {
        return this.applicationInstanceList;
    }

    @Override
    public List<ApplicationInstance> getApplicationInstanceList() {
        return this.applicationInstanceList;
    }

    public void setApplicationInstanceList(List<ApplicationInstance> applicationInstanceList) {
        this.applicationInstanceList = applicationInstanceList;
    }

    public void setZone(String zone) {
        this.zone = new DefaultZone();
        this.zone.setName(zone);

        DefaultZoneVersion zoneVersion = new DefaultZoneVersion();
        zoneVersion.setId(1);
        zoneVersion.setPrevVersionId(0);
        this.zone.setVersion(1);
    }

    public void setApplicationList(List<String> applicationList) {
        this.applicationList = applicationList;
    }
}
