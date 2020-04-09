/**
 * Copyright 2019 Jonas Yang
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yanglinkui.grass;

import com.yanglinkui.grass.exception.GrassException;

import java.io.Serializable;
import java.lang.reflect.Type;

/**
 * 规定错误信息格式
 */
public class Error implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;

    private String message;

    public Error() {}

    public Error(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Error(Throwable e) {
        if (e == null) {
            throw new IllegalArgumentException("The e cannot be null");
        }
        if (e instanceof GrassException) {
            this.code = ((GrassException) e).getCode();
        } else {
            this.code = GrassResponse.INTERNAL_ERROR;
        }

        this.message = e.getMessage();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
