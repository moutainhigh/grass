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
import com.yanglinkui.grass.provider.ServiceProcessor;
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.exception.GrassException;
import com.yanglinkui.grass.exception.InvokedException;

public class CheckRequestProcessor implements ServiceProcessor {

    private final String applicationName;

    private final String zone;

    public CheckRequestProcessor(String zone, String applicationName) {
        this.zone = zone;
        this.applicationName = applicationName;
    }

    @Override
    public GrassResponse process(Chain<GrassResponse> chain) {
        GrassRequest request = chain.getRequest();

        if (!this.applicationName.equals(request.getApplicationName())) {
            throw newInvokedException("The requested application("
                    + request.getApplicationName()
                    + ") is different from "
                    + this.applicationName);

        }

        if (!zone.equals(request.getZone())) {
            throw newInvokedException("The requested zone("
                    + request.getZone() + " of "
                    + request.getApplicationName()
                    + ") is different from " + this.zone);
        }

        if (Utils.isEmpty(request.getServiceId())) {
            throw newInvokedException("No service Id of request: " + request.getId());
        }

        return chain.proceed(request);
    }

    GrassException newInvokedException(String message) {
        GrassException exception = new InvokedException(message);
        exception.setFillInStackTrace(false);

        return exception;
    }
}
