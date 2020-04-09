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

package com.yanglinkui.grass.client;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.mock.MockDefaultApplicationInstance;
import com.yanglinkui.grass.mock.MockProtocolClient;
import com.yanglinkui.grass.mock.MockProtocolFactory;
import com.yanglinkui.grass.registry.Registry;
import static org.junit.jupiter.api.Assertions.*;

import com.yanglinkui.grass.registry.DefaultZone;
import com.yanglinkui.grass.registry.DefaultZoneVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

public class DefaultRouterTest {

    MockRegistry registry;

    @BeforeEach
    public void init() {
        registry = new MockRegistry();

        String prefix = "zone";
        String parent = "default";
        for (int i = 1; i < 9; i++) {
            DefaultZone zone = new DefaultZone();
            zone.setName(prefix + i);
            zone.setParentZoneName(parent);
            registry.addZone(zone);

//            System.out.println(zone.getName() + ": ");
            for (int j = 10; j > 0; j--) {
                DefaultZoneVersion version = new DefaultZoneVersion();
                version.setId(j);
                version.setPrevVersionId(j/2);
                registry.addZoneVersion(zone.getName(), version);

//                System.out.println("    " + version.getId() + "， Prev: " + version.getPrevVersionId() + ":");
                for (int k = 15; k > j; k--) {
                    MockDefaultApplicationInstance instance = new MockDefaultApplicationInstance(zone.getName(), "app-" + k, MockProtocolFactory.ID);
                    instance.setZoneVersion(version.getId());
                    registry.addApplicationInstance(instance);
//                    System.out.println("        " + instance.getApplicationName());
                }
            }

            if (i % 3 == 0) {
                parent = zone.getName();
            }
        }

        ApplicationInstance instance = new MockDefaultApplicationInstance("default", "app-jonas", MockProtocolFactory.ID);
        registry.addApplicationInstance(instance);

        DefaultZone zone = new DefaultZone();
        zone.setName("default");
        registry.addZone(zone);

    }

    @Test
    public void testCheckRequestZoneVersion() {
        DefaultGrassRequest request = new DefaultGrassRequest();

        DefaultRouter router = new DefaultRouter(null);
        GrassRequest grassRequest = router.checkRequestZone(request, 1, "test");
        assertEquals(1, grassRequest.getZoneVersion());
        assertEquals("test", grassRequest.getZone());
    }

    @Test
    public void testIsBefore() {
        DefaultRouter router = new DefaultRouter(null);
        assertTrue(router.isBefore(1, 2));
        assertFalse(router.isBefore(2, 1));
        assertFalse(router.isBefore(3, 3));
    }

    @Test
    public void testIsEqual() {
        DefaultRouter router = new DefaultRouter(null);
        assertTrue(router.isEqual(3, 3));
        assertFalse(router.isEqual(1, 2));
        assertFalse(router.isEqual(2, 1));
    }

    @Test
    public void testIsAfter() {
        DefaultRouter router = new DefaultRouter(null);
        assertTrue(router.isAfter(2, 1));
        assertFalse(router.isAfter(1, 2));
        assertFalse(router.isAfter(3, 3));
    }

    @Test
    public void testGetApplicationInstanceListOfZone() {
        DefaultRouter router = new DefaultRouter(registry);
        Zone zone = registry.getZone("zone8");
        assertNotNull(zone);
        List<ApplicationInstance> list = router.getApplicationInstanceListOfZone(zone, "app-8");
        assertEquals(7, list.size());

        list = router.getApplicationInstanceListOfZone(zone, "app-jonas");
        assertEquals(1, list.size());
    }

    @Test
    public void testGetApplicationInstanceByVersion() {
        DefaultRouter router = new DefaultRouter(registry);
        Zone zone = registry.getZone("zone8");
        ZoneVersion zoneVersion = registry.getZoneVersion(zone.getName(), 10);
        List<ApplicationInstance> list = router.getApplicationInstanceByVersion(zoneVersion, zone, "app-8");
        assertEquals(3, list.size());

        zone = registry.getZone("zone1");
        zoneVersion = registry.getZoneVersion(zone.getName(), 7);
        list = router.getApplicationInstanceByVersion(zoneVersion, zone, "app-15");
        assertEquals(3, list.size());

        zone = registry.getZone("zone1");
        zoneVersion = registry.getZoneVersion(zone.getName(), 9);
        list = router.getApplicationInstanceByVersion(zoneVersion, zone, "app-jonas");
        assertEquals(1, list.size());

    }

    static class MockRegistry implements Registry {
        Map<String, Zone> zoneList = new HashMap<>();
        Map<String, ZoneVersion> zoneVersionList = new HashMap<>();

        Map<String, Map<String, List<ApplicationInstance>>> allInstanceList = new HashMap<>();

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
            return zoneList.get(name);
        }

        @Override
        public List<String> getApplicationList(String spi) {
            return null;
        }

        @Override
        public List<ApplicationInstance> getApplicationInstanceList(String zone, String applicationName) {
            Map<String, List<ApplicationInstance>> zoneInstanceList = allInstanceList.get(zone);
            if (zoneInstanceList == null) {
                return null;
            }
            return zoneInstanceList.get(applicationName);
        }

        @Override
        public List<ApplicationInstance> getApplicationInstanceList() {
            return null;
        }

        @Override
        public ZoneVersion getZoneVersion(String zone, Integer zoneVersionId) {
            return zoneVersionList.get(zone + "+verison: " + zoneVersionId);
        }

        public void addZone(Zone zone) {
            this.zoneList.put(zone.getName(), zone);
        }

        public void addZoneVersion(String zone, ZoneVersion version) {
            this.zoneVersionList.put(zone + "+verison: " + version.getId(), version);
        }

        public void addApplicationInstance(ApplicationInstance applicationInstance) {
            Map<String, List<ApplicationInstance>> zoneInstanceList = allInstanceList.get(applicationInstance.getZone());
            if (zoneInstanceList == null) {
                zoneInstanceList = new HashMap<>();
                allInstanceList.put(applicationInstance.getZone(), zoneInstanceList);
            }

            List<ApplicationInstance> list = zoneInstanceList.get(applicationInstance.getApplicationName());
            if (list == null) {
                list = new ArrayList<>();
                zoneInstanceList.put(applicationInstance.getApplicationName(), list);
            }

            list.add(applicationInstance);
            Collections.sort(list);
            zoneInstanceList.put(applicationInstance.getApplicationName(), list);
        }
    }

}
