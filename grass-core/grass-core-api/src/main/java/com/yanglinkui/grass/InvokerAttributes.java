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

import com.yanglinkui.grass.common.Utils;

import java.lang.reflect.Type;

public class InvokerAttributes {

    private final String id;

    private final String action;

    private final String version;

    private final long timeout;

    private transient final ParameterAttribute[] parameterAttributes;

    private final Type returnType;

    private final Class returnTypeClass;

    public InvokerAttributes(String id, String action, String version, long timeout, ParameterAttribute[] parameterAttributes, Type returnType, Class returnTypeClass) {
        if (Utils.isEmpty(id)) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (parameterAttributes == null) {
            throw new IllegalArgumentException("parameterAttributes cannot be null");
        }

        if (returnType == null) {
            throw new IllegalArgumentException("returnType cannot be null");
        }

        if (returnTypeClass == null) {
            throw new IllegalArgumentException("returnTypeClass cannot be null");
        }

        this.id = id;
        this.action = (action == null ? "" : action);
        this.version = (version == null ? "" : version);
        this.timeout = (timeout < 0 ? 0 : timeout);
        this.parameterAttributes = parameterAttributes;
        this.returnType = returnType;
        this.returnTypeClass = returnTypeClass;
    }

    public String getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getVersion() {
        return version;
    }

    public long getTimeout() {
        return timeout;
    }

    public ParameterAttribute[] getParameterAttributes() {
        return parameterAttributes;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Class getReturnTypeClass() {
        return returnTypeClass;
    }
}
