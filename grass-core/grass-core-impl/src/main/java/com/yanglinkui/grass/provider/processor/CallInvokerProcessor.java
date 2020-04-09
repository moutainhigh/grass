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

package com.yanglinkui.grass.provider.processor;

import com.yanglinkui.grass.*;
import com.yanglinkui.grass.exception.InvokedException;
import com.yanglinkui.grass.DefaultGrassResponse;
import com.yanglinkui.grass.provider.ServiceInvoker;
import com.yanglinkui.grass.provider.ServiceProcessor;

public class CallInvokerProcessor implements ServiceProcessor {

    @Override
    public GrassResponse process(Chain<GrassResponse> chain) {
        GrassRequest request = chain.getRequest();
        ServiceInvoker invoker = request.getAttribute(ProcessorConstants.Request.Attribute.GRASS_INVOKER);

        if (invoker == null) {
            throw new InvokedException("No invoker, request: " + request.getId());
        }

        Object[] args = request.getAttribute(ProcessorConstants.Request.Attribute.GRASS_INVOKER_ARGS);
        if (args == null) {
            throw new InvokedException("No invoker's arguments, request: " + request.getId());
        }

        try {
            Object result = invoker.invoke(args);
            DefaultGrassResponse response = new DefaultGrassResponse();
            response.setStatus(GrassResponse.SUCCESS);
            response.setBody(result);
            response.setServiceId(invoker.getAttributes().getId());
            response.setAction(invoker.getAttributes().getAction());
            response.setVersion(invoker.getAttributes().getVersion());
            return response;
        } catch (Throwable ex) {
            throw new InvokedException("Failed to invoke: " + request.getServiceId() + ", request: " + request.getId(), ex);
        }

    }
}
