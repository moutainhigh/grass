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

package com.yanglinkui.grass.common;

import java.util.Optional;
import java.util.function.Supplier;

public class StringOptional {

    private final Optional<String> optional;

    public StringOptional(Optional<String> optional) {
        this.optional = optional;
    }

    public static StringOptional of(String val) {
        val = Utils.isEmpty(val) ? null : val;
        Optional<String> optional = Optional.ofNullable(val);
        return new StringOptional(optional);
    }

    public String orElse(String val) {
        return this.optional.orElseGet(() -> val);
    }

    public String orElse(Supplier<String> other) {
        return this.optional.orElseGet(other);
    }

    public <X extends Throwable> String orElseThrow(Supplier<? extends X> e) throws Throwable {
        return this.optional.orElseThrow(e);
    }

}
