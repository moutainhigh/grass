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

package com.yanglinkui.grass.protocol.message;

import com.yanglinkui.grass.Invoker;
import com.yanglinkui.grass.provider.Service;

import java.util.List;

public class MockService implements Service {

    private final Object obj;

    private final Type type;

    private final List<Invoker> invokerList;

    public MockService(Object obj, Type type, List<Invoker> invokerList) {
        this.obj = obj;
        this.type = type;
        this.invokerList = invokerList;
    }

    @Override
    public Object getObject() {
        return this.obj;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public List<Invoker> getInvokerList() {
        return this.invokerList;
    }
}
