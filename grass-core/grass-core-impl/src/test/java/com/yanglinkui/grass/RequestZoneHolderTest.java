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

import static org.junit.jupiter.api.Assertions.*;

import com.yanglinkui.grass.zone.RequestZone;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

public class RequestZoneHolderTest {

    @Test
    public void testFocusDestroy() {
        RequestZoneHolder holder1 = RequestZoneHolder.getInstance("zone1");
        RequestZoneHolder.getInstance();
        RequestZoneHolder.getInstance();
        RequestZoneHolder.getInstance();
        RequestZoneHolder.getInstance();
        RequestZoneHolder.getInstance();
        RequestZoneHolder.getInstance();

        holder1.destroy();
        assertNotNull(RequestZoneHolder.getInstance());

        holder1.destroy(true);
        assertNull(RequestZoneHolder.getInstance());
    }

    @Test
    public void testGetInstanceByString() {
        RequestZoneHolder holder1 = RequestZoneHolder.getInstance("zone1");
        RequestZoneHolder holder2 = RequestZoneHolder.getInstance("zone2");

        assertNotEquals(holder1, holder2);

        holder2.destroy(true);

        assertNull(RequestZoneHolder.getInstance());
    }

    @Test
    public void testGetInstanceNoParemeter() throws InterruptedException {
        RequestZoneHolder holder1 = RequestZoneHolder.getInstance("zone1");
        assertEquals(holder1, RequestZoneHolder.getInstance());
        final CountDownLatch latch = new CountDownLatch(1);
        new Thread() {
            @Override
            public void run() {
                assertNull(RequestZoneHolder.getInstance());
                latch.countDown();
            }
        }.start();

        latch.await();
        holder1.destroy(true);


    }

    @Test
    public void testGetInstanceByRequestZone() {
        RequestZoneHolder holder0 = RequestZoneHolder.getInstance("zone1");
        RequestZone zone = holder0.getRequestZone();
        RequestZoneHolder holder1 = RequestZoneHolder.getInstance(zone);
        RequestZoneHolder holder2 = RequestZoneHolder.getInstance(zone);

        assertNotEquals(holder1, holder2);
        assertEquals(holder1.getRequestZone(), holder2.getRequestZone());

        holder2.destroy(true);

        assertNull(RequestZoneHolder.getInstance());
    }

    @Test
    public void testDestroy() {
        RequestZoneHolder holder = RequestZoneHolder.getInstance("zone1");
        holder.destroy();
        assertNull(RequestZoneHolder.getInstance());

        holder = RequestZoneHolder.getInstance("zone1");
        RequestZoneHolder holder1 = RequestZoneHolder.getInstance();
        assertEquals(holder, holder1);
        holder1.destroy();
        assertNotNull(RequestZoneHolder.getInstance());
        holder.destroy();
        holder.destroy();
        assertNull(RequestZoneHolder.getInstance());
    }
}
