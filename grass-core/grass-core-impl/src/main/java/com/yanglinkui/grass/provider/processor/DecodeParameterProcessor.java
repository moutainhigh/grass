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
import com.yanglinkui.grass.provider.ServiceInvoker;
import com.yanglinkui.grass.provider.ServiceProcessor;

import java.util.Collections;
import java.util.Map;

public class DecodeParameterProcessor implements ServiceProcessor {

    @Override
    public GrassResponse process(Chain<GrassResponse> chain) {
        GrassRequest request = chain.getRequest();
        //TODO: 魔术代码，依赖上一个拦截器，怎么处理更好呢？
        ServiceInvoker invoker = request.getAttribute("GRASS-INVOKER");
        InvokerAttributes properties = invoker.getAttributes();
        ParameterAttribute[] parameterAttributes = properties.getParameterAttributes();

        Map<String, Object> parameterMap = request.getBody();
        if (parameterMap == null || parameterMap.size() == 0) {
            RawInputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                try {
                    parameterMap = inputStream.deserialize(parameterAttributes);
                    request = updateRequest(request, parameterMap);
                } catch (Exception e) {
                    throw new InvokedException("Failed to deserialize request: " + request.getId());
                }
            }
        }

        if (parameterMap == null) {
            parameterMap = Collections.EMPTY_MAP;
        }

        Object[] args = new Object[parameterAttributes.length];
        for (ParameterAttribute attribute : parameterAttributes) {
            args[attribute.getIndex()] = parameterMap.get(attribute.getName());
        }

        //转换好后放到下一级
        request.setAttribute(ProcessorConstants.Request.Attribute.GRASS_INVOKER_ARGS, args);
        return chain.proceed(request);

    }

    GrassRequest updateRequest(GrassRequest request, final Map<String, Object> parameterMap) {
        if (parameterMap != null && parameterMap.size() > 0) {
            request = new GrassRequestWrapper(request) {
                @Override
                public Map<String, Object> getBody() {
                    return Collections.unmodifiableMap(parameterMap);
                }
            };
        }
        return request;
    }
}
