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

package com.yanglinkui.grass.exception;

import com.yanglinkui.grass.GrassResponse;

public class GrassException extends RuntimeException {

    protected int code = GrassResponse.INTERNAL_ERROR;

    protected boolean isFillInStackTrace = true;

    public GrassException() {
    }

    public GrassException(String message) {
        super(message);
    }

    public GrassException(String message, Throwable cause) {
        super(message, cause);
    }

    public GrassException(Throwable cause) {
        super(cause);
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    //是否创建轻量级的Exception
    public void setFillInStackTrace(boolean isFillInStackTrace) {
        this.isFillInStackTrace = isFillInStackTrace;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (!isFillInStackTrace) {
            return null;
        }
        return super.fillInStackTrace();
    }
}
