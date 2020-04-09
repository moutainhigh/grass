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

import com.yanglinkui.grass.GrassRequest;
import com.yanglinkui.grass.Processor;

import java.util.function.Function;

public class MockProcessorChain<T> implements Processor.Chain<T> {

    private GrassRequest request;

    private T result;

    private Function<GrassRequest, T> handle;

    public MockProcessorChain(GrassRequest request, T result) {
        this.request = request;
        this.result = result;
    }

    public MockProcessorChain(GrassRequest request, Function<GrassRequest, T> handle) {
        this.request = request;
        this.handle = handle;
    }

    @Override
    public GrassRequest getRequest() {
        return request;
    }

    @Override
    public T proceed(GrassRequest request) {
        if (handle != null) {
            return handle.apply(request);
        }
        return result;
    }
}
