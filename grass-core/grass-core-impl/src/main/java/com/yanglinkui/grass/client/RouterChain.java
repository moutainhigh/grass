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

package com.yanglinkui.grass.client;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.exception.GrassStateException;

import java.util.List;

public class RouterChain implements Router.Chain {

    private final GrassRequest request;

    private final List<ApplicationInstance> applicationInstanceList;

    private final List<Router> routerList;

    private final int index;

    public RouterChain(GrassRequest request, List<ApplicationInstance> applicationInstanceList, List<Router> routerList) {
        this(request, applicationInstanceList, routerList, 0);
    }

    public RouterChain(GrassRequest request, List<ApplicationInstance> applicationInstanceList, List<Router> routerList, int index) {
        this.request = request;
        this.routerList = routerList;
        this.applicationInstanceList = applicationInstanceList;
        this.index = index;
    }

    public GrassRequest getRequest() {
        return this.request;
    }

    public List<ApplicationInstance> getApplicationInstanceList() {
        return this.applicationInstanceList;
    }

    public List<ApplicationInstance> proceed(GrassRequest request, List<ApplicationInstance> list) {
        if (list == null || list.size() == 0) {
            throw new IllegalArgumentException("ApplicationInstance list cannot be null");
        }

        if (this.index >= routerList.size()) {
            throw new ArrayIndexOutOfBoundsException("index is " + this.index + " that is greater than or equals " + this.routerList.size());
        }

        RouterChain next = new RouterChain(request, list, this.routerList, this.index + 1);
        Router router = this.routerList.get(this.index);

        return router.choose(next);
    }
}
