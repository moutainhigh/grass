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

package com.yanglinkui.grass.protocol.message;

import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.protocol.ServerProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultServerProperties implements ServerProperties {

    private final String addresses;
    private final String authorization;
    private final String protocol;

    public final Map<String, String> metadata = new HashMap<>();

    public DefaultServerProperties(String protocol, String addresses, String authorization) {
        this.protocol = protocol;
        this.addresses = addresses;
        this.authorization = authorization;
    }

    @Override
    public String getAddresses() {
        return this.addresses;
    }

    @Override
    public String protocol() {
        return this.protocol;
    }

    @Override
    public String getAuthorization() {
        return this.authorization;
    }

    @Override
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(this.metadata);
    }

    public void addMetadata(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }

        if (Utils.isEmpty(value)) {
            return;
        }
        this.metadata.put(name, value);
    }
}
