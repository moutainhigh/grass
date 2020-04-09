/*
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

import com.yanglinkui.grass.AbstractProcessorChain;
import com.yanglinkui.grass.GrassRequest;
import com.yanglinkui.grass.GrassResponse;
import com.yanglinkui.grass.Processor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientProcessorChain extends AbstractProcessorChain<CompletableFuture<GrassResponse>> {

    public ClientProcessorChain(GrassRequest request, List<ClientProcessor> list) {
        this(request, list, 0);
    }

    public ClientProcessorChain(GrassRequest request, List<ClientProcessor> list, int index) {
        super(request, list, index);
    }

    @Override
    protected Processor.Chain<CompletableFuture<GrassResponse>> createNextChain(GrassRequest request, List<? extends Processor<CompletableFuture<GrassResponse>>> list, int index) {
        return new ClientProcessorChain(request, (List<ClientProcessor>) list, index);
    }
}
