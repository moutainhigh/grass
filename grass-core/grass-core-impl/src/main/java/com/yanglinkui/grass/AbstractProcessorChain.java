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

import java.util.List;

public abstract class AbstractProcessorChain<T> implements Processor.Chain<T> {

    protected final List<? extends Processor<T>> list;

    protected final GrassRequest request;

    protected final int index;

    public AbstractProcessorChain(GrassRequest request, List<? extends Processor<T>> list) {
        this(request, list, 0);
    }

    public AbstractProcessorChain(GrassRequest request, List<? extends Processor<T>> list, int index) {
        this.request = request;
        this.list = list;
        this.index = index;
    }

    @Override
    public T proceed(GrassRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request cannot be null");
        }

        if (this.index >= this.list.size()) {
            throw new ArrayIndexOutOfBoundsException("index is " + this.index + " that is greater than or equals " + this.list.size());
        }

        Processor.Chain<T> next = createNextChain(request, this.list, this.index + 1);
        Processor<T> processor = this.list.get(this.index);
        return processor.process(next);
    }

    protected abstract Processor.Chain<T> createNextChain(GrassRequest request, List<? extends Processor<T>> list, int index);

    @Override
    public GrassRequest getRequest() {
        return this.request;
    }

}
