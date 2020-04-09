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

public class RequestZoneParserTest {

    @Test
    public void testRequestZone() {
        String value1 = "zone1(  application1, application.2, application-3, application_4)    ";
        RequestZoneParser parser = new RequestZoneParser(value1);

        RequestZone zone = parser.requestZone();

        assertEquals("zone1", zone.getZoneList().get(0).getId());
        assertEquals(zone.getValue(), value1);
        assertTrue(zone.contains("application1"));
        assertTrue(zone.contains("application.2"));
        assertTrue(zone.contains("application-3"));
        assertTrue(zone.contains("application_4"));
        assertTrue(zone.contains("application3") == false);

        String value2 = "zone2()";
        RequestZone zone2 = new RequestZoneParser(value2).requestZone();
        assertEquals(zone2.getZoneList().get(0).getId(), "zone2");
        assertEquals(zone2.getValue(), value2);
        assertTrue(zone2.contains("application1") == false);

        String value3 = "zone3";
        RequestZone zone3 = new RequestZoneParser(value3).requestZone();
        assertEquals(zone3.getZoneList().get(0).getId(), "zone3");
        assertEquals(zone3.getValue(), value3);
        assertTrue(zone3.contains("application1") == false);

        //错误语法测试
        try {
            RequestZone zone4 = new RequestZoneParser("zone4;").requestZone();
            fail("zone4; should be failed.");
        } catch (Error e) {

        }

        try {
            RequestZone zone5 = new RequestZoneParser("zone5(").requestZone();
            fail("zone5( should be failed.");
        } catch (Error e) {
        }

        try {
            RequestZone zone6 = new RequestZoneParser("zone6)").requestZone();
            fail("zone6) should be failed.");
        } catch (Error e) {

        }

        try {
            RequestZone zone7 = new RequestZoneParser("zone7(application;)").requestZone();
            fail("zone7(application;) should be failed.");
        } catch (Error e) {

        }
    }

    @Test
    public void testEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> {
            RequestZoneParser parser = new RequestZoneParser(null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            RequestZoneParser parser = new RequestZoneParser("");
        });
    }

    @Test
    public void testOfString() {
        assertNull(RequestZoneParser.parse(""));
        assertNull(RequestZoneParser.parse(null));
        assertNotNull(RequestZoneParser.parse("zone"));
    }

    @Test
    public void testMultiZones() {
        String value1 = "zone1(  application1, application.2), zone2(application-3)    ";
        RequestZoneParser parser = new RequestZoneParser(value1);

        RequestZone zone = parser.requestZone();

        assertEquals(2, zone.getZoneList().size());
        assertEquals("zone1", zone.getZoneList().get(0).getId());
        assertEquals(2, zone.getZoneList().get(0).getApplicationSize());
        assertEquals("zone2", zone.getZoneList().get(1).getId());
        assertEquals(1, zone.getZoneList().get(1).getApplicationSize());
        assertEquals(zone.getValue(), value1);
        assertTrue(zone.contains("application1"));
        assertTrue(zone.contains("application.2"));
        assertTrue(zone.contains("application-3"));
        assertTrue(zone.contains("application3") == false);

        String value2 = "zone1, zone3(application1)";
        RequestZone zone2 = RequestZoneParser.parse(value2);

        assertEquals(2, zone2.getZoneList().size());
        assertEquals("zone1", zone2.getZoneList().get(0).getId());
        assertEquals(0, zone2.getZoneList().get(0).getApplicationSize());
        assertEquals("zone3", zone2.getZoneList().get(1).getId());
        assertEquals(1, zone.getZoneList().get(1).getApplicationSize());
        assertEquals(zone.getValue(), value1);
        assertTrue(zone.contains("application1"));

    }

}
