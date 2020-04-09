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

package com.yanglinkui.grass.zone;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestZoneTest {

    @Test
    public void testGetZone() {
        List<RequestZone.Zone> zoneList = new ArrayList<>();
        zoneList.add(new RequestZone.Zone("zone1", 1));
        zoneList.add(new RequestZone.Zone("zone2", 1));

        Map<String, String> applicationList = new HashMap<>();
        applicationList.put("app1", "zone1");
        applicationList.put("app2", "zone2");
        RequestZone requestZone = new RequestZone(zoneList, applicationList, "zone1(app1), zone2(app2)");

        assertEquals("zone1", requestZone.getZone("app1"));
        assertEquals("zone2", requestZone.getZone("app2"));
    }
}
