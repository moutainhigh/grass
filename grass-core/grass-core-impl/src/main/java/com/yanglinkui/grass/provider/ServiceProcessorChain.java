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

package com.yanglinkui.grass.provider;

import com.yanglinkui.grass.AbstractProcessorChain;
import com.yanglinkui.grass.GrassRequest;
import com.yanglinkui.grass.GrassResponse;
import com.yanglinkui.grass.Processor;

import java.util.List;

public class ServiceProcessorChain extends AbstractProcessorChain<GrassResponse> {

    public ServiceProcessorChain(GrassRequest request, List<ServiceProcessor> list) {
        this(request, list, 0);
    }

    public ServiceProcessorChain(GrassRequest request, List<ServiceProcessor> list, int index) {
        super(request, list, index);
    }

    @Override
    protected Processor.Chain<GrassResponse> createNextChain(GrassRequest request, List<? extends Processor<GrassResponse>> list, int index) {
        return new ServiceProcessorChain(request, (List<ServiceProcessor>)list, this.index + 1);
    }
}
