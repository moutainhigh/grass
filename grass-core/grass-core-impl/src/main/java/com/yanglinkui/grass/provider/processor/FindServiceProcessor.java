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

import com.yanglinkui.grass.GrassRequest;
import com.yanglinkui.grass.GrassResponse;
import com.yanglinkui.grass.Invoker;
import com.yanglinkui.grass.Repository;
import com.yanglinkui.grass.provider.Service;
import com.yanglinkui.grass.provider.ServiceProcessor;

public class FindServiceProcessor implements ServiceProcessor {

    private final Repository<Service> repository;

    public FindServiceProcessor(Repository<Service> repository) {
        this.repository = repository;
    }

    @Override
    public GrassResponse process(Chain<GrassResponse> chain) {
        GrassRequest request = chain.getRequest();
        Invoker invoker = this.repository.getInvoker(request.getServiceId(), request.getAction(), request.getServiceVersion());

        //检索好的invoker放到attribute，传给下一个interceptor
        request.setAttribute(ProcessorConstants.Request.Attribute.GRASS_INVOKER, invoker);

        return chain.proceed(request);
    }


}
