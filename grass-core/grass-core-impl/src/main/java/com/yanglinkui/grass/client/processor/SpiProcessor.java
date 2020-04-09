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

package com.yanglinkui.grass.client.processor;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.client.ClientProcessor;
import com.yanglinkui.grass.client.SpiRouter;
import com.yanglinkui.grass.exception.InvokedException;
import com.yanglinkui.grass.registry.Registry;
import com.yanglinkui.grass.common.Utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SpiProcessor implements ClientProcessor {

    private final SpiRouter spiRouter;

    private final Registry registry;

    public SpiProcessor(SpiRouter spiRouter, Registry registry) {
        this.spiRouter = spiRouter;
        this.registry = registry;
    }

    @Override
    public CompletableFuture<GrassResponse> process(Chain<CompletableFuture<GrassResponse>> chain) {
        GrassRequest request = chain.getRequest();

        //SPI 路由
        if (Utils.isEmpty(request.getApplicationName())) {
            final String applicationName = getApplication(request);
            request = new GrassRequestWrapper(request) {
                @Override
                public String getApplicationName() {
                    return applicationName;
                }
            };
        }

        return chain.proceed(request);
    }

    String getApplication(GrassRequest request) {
        if (this.spiRouter == null) {
            throw new InvokedException("Not found SpiRouter for the spi(" + request.getSpi() + ")");
        }
        List<String> applicationList = registry.getApplicationList(request.getSpi());
        String applicationName = spiRouter.choose(request.getSpi(), applicationList);
        if (Utils.isEmpty(applicationName)) {
            throw new InvokedException("Not found application implementation of the spi(" + request.getSpi() + "), please check the spi spiRouter");
        }
        return applicationName;
    }
}
