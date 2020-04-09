/**
 * Copyright 2019 Jonas Yang
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yanglinkui.grass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock 语法
 * TODO: class(1,2,3),application(1,2,3),spi(1,2,3)
 */
public class MockFlagHolder {

    private final static Object VALUE = new Object();

    private final static ThreadLocal<MockFlagHolder> INSTANCES = new ThreadLocal<>();

    private final Map<String, Object> applicationList;

    private int invokedCount = 0;

    private MockFlagHolder(Map<String, Object> applicationList) {
        this.applicationList = applicationList;
    }

    public static MockFlagHolder getInstance(String value) {
        String[] applications = value.split(",");

        Map<String, Object> applicationList = new ConcurrentHashMap<>(16, 1F);
        for (String application : applications) {
            applicationList.put(application.trim(), VALUE);
        }

        INSTANCES.set(new MockFlagHolder(applicationList));
        return INSTANCES.get();
    }

    public static MockFlagHolder getInstance(Map<String, Object> applicationList) {
        INSTANCES.set(new MockFlagHolder(applicationList));
        return INSTANCES.get();
    }

    public static MockFlagHolder getInstance() {
        MockFlagHolder holder = INSTANCES.get();
        if (holder != null) {
            holder.invokedCount++;
        }

        return holder;
    }

    public void destroy() {
        if (this.invokedCount == 0) {
            INSTANCES.remove();
        } else {
            this.invokedCount--;
        }
    }

    public void destroy(boolean forced) {
        if (forced) {
            INSTANCES.remove();
        } else {
            destroy();
        }
    }
}
