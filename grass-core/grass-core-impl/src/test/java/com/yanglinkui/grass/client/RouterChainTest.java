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

import com.yanglinkui.grass.ApplicationInstance;
import com.yanglinkui.grass.GrassRequest;
import com.yanglinkui.grass.DefaultGrassRequest;
import com.yanglinkui.grass.exception.InvokedException;
import static org.junit.jupiter.api.Assertions.*;

import com.yanglinkui.grass.mock.MockDefaultApplicationInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class RouterChainTest {

    List<ApplicationInstance> applicationInstanceList = new ArrayList<>();

    List<Router> routerList = new ArrayList<>();

    @BeforeEach
    public void init() {
        applicationInstanceList.add(new MockDefaultApplicationInstance("app1", "message"));
        applicationInstanceList.add(new MockDefaultApplicationInstance("app2", "message"));
        applicationInstanceList.add(new MockDefaultApplicationInstance("app3", "message"));
        applicationInstanceList.add(new MockDefaultApplicationInstance("app4", "message"));
        applicationInstanceList.add(new MockDefaultApplicationInstance("app5", "message"));
        applicationInstanceList.add(new MockDefaultApplicationInstance("app6", "message"));
        applicationInstanceList.add(new MockDefaultApplicationInstance("app7", "message"));

        routerList.add(new TestRouter(false));
        routerList.add(new TestRouter(false));
        routerList.add(new TestRouter(false));
        routerList.add(new TestRouter(true));
        routerList.add(new TestRouter(false));
        routerList.add(new TestRouter(false));
        routerList.add(new TestRouter(false));
        routerList.add(new TestRouter(false));
    }

    @Test
    public void testChain() {
        GrassRequest request = new DefaultGrassRequest();
        RouterChain chain = new RouterChain(request, applicationInstanceList, routerList);
        List<ApplicationInstance> result = chain.proceed(request, applicationInstanceList);
        assertEquals(1, result.size());
        assertEquals(3, TestRouter.i);
        assertEquals(applicationInstanceList.get(TestRouter.i), result.get(0));
    }

    @Test
    public void testChain_throws_exception() {
        List<ApplicationInstance> applicationInstanceList = new ArrayList<>();
        GrassRequest request = new DefaultGrassRequest();
        RouterChain chain = new RouterChain(request, applicationInstanceList, routerList);
        assertThrows(IllegalArgumentException.class, () -> chain.proceed(request, applicationInstanceList));
    }

    private static class TestRouter implements Router {
        public static int i = 0;

        private boolean isEnd = false;

        public TestRouter(boolean isEnd) {
            this.isEnd = isEnd;
        }

        @Override
        public List<ApplicationInstance> choose(Chain chain) {
            List<ApplicationInstance> list = chain.getApplicationInstanceList();
            if (list.size() == 0) {
                throw new InvokedException("No application instance");
            }

            if (this.isEnd) {
                List<ApplicationInstance> applicationInstanceList = new ArrayList<>();
                applicationInstanceList.add(list.get(i));
                return applicationInstanceList;
            }

            i++;
            return chain.proceed(chain.getRequest(), list);
        }
    }

}
