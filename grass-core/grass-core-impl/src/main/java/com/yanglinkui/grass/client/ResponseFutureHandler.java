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

package com.yanglinkui.grass.client;

import com.yanglinkui.grass.Error;
import com.yanglinkui.grass.GrassResponse;
import com.yanglinkui.grass.RawInputStream;
import com.yanglinkui.grass.exception.InvokedException;

import java.lang.reflect.Type;

public class ResponseFutureHandler<T> {

    private final Type returnType;

    public ResponseFutureHandler(Type returnType) {
        this.returnType = returnType;
    }

    public T handle(GrassResponse response, Throwable throwable) {
        if (throwable != null) {
            throw new InvokedException(throwable);
        }

        Object result = response.getBody();
        if (response.getStatus() == GrassResponse.SUCCESS) {
            if (result != null) {
                return (T) result;
            }

            return deserialize(response, this.returnType);
        } else {
            Error error = (Error) result;
            if (result == null) {
                error = deserialize(response, Error.class);
            }

            InvokedException exception = new InvokedException(error == null ? "Internal error" : error.getMessage());
            exception.setCode(error == null ? response.getStatus() : error.getCode());

            throw exception;
        }
    }

    private <T> T deserialize(GrassResponse response, Type type) {
        RawInputStream inputStream = response.getInputStream();
        if (inputStream == null) {
            return null;
        } else {
            try {
                return inputStream.deserialize(type);
            } catch (Exception e) {
                throw new InvokedException("Failed to deserialize response body", e);
            }
        }
    }
}
