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
import com.yanglinkui.grass.exception.InvokedException;
import com.yanglinkui.grass.protocol.Client;
import com.yanglinkui.grass.protocol.ProtocolFactory;
import com.yanglinkui.grass.protocol.ProtocolFactoryManager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CallServiceProcessor implements ClientProcessor {

    private final GrassContext context;

    private final long timeout;

    public CallServiceProcessor(GrassContext context, long timeout) {
        this.context = context;
        this.timeout = timeout;
    }

    @Override
    public CompletableFuture<GrassResponse> process(Chain<CompletableFuture<GrassResponse>> chain) {
        GrassRequest request = chain.getRequest();
        //通过instance信息获得具体应该使用哪个协议
        ApplicationInstance applicationInstance = Optional.ofNullable(
                (ApplicationInstance) request.getAttribute(ProcessorConstants.Request.Attribute.APPLICATION_INSTANCE)
        ).orElseThrow(() -> new InvokedException("No application instance to " + request.getApplicationName()));

        Client client = Optional.ofNullable(this.context.getProtocol(applicationInstance.getProtocol()))
                .orElseThrow(() -> new InvokedException("Not found protocol(" + applicationInstance.getProtocol() + ") client"));

        CompletableFuture<GrassResponse> response = client.invoke(applicationInstance, request, this.timeout);
        return response;
    }
}
