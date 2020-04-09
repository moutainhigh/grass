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

public class NotFoundException extends GrassException {

    public NotFoundException() {
        super.code = GrassResponse.NOT_FOUND;
    }

    public NotFoundException(String message) {
        super(message);
        super.code = GrassResponse.NOT_FOUND;
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
        super.code = GrassResponse.NOT_FOUND;
    }

    public NotFoundException(Throwable cause) {
        super(cause);
        super.code = GrassResponse.NOT_FOUND;
    }
}
