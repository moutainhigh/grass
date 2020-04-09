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

import com.yanglinkui.grass.ApplicationInstance;
import com.yanglinkui.grass.GrassRequest;
import com.yanglinkui.grass.GrassResponse;
import com.yanglinkui.grass.protocol.Client;

import java.util.concurrent.CompletableFuture;

public class MockProtocolClient implements Client {

    private CompletableFuture<GrassResponse> result;

    public MockProtocolClient(CompletableFuture<GrassResponse> result) {
        this.result = result;
    }

    @Override
    public CompletableFuture<GrassResponse> invoke(ApplicationInstance applicationInstance, GrassRequest request, long timeout) {
        return result;
    }
}
