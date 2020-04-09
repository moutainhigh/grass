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

import com.yanglinkui.grass.protocol.ServerInfo;
import com.yanglinkui.grass.protocol.ServerProperties;

public class DefaultServerInfo implements ServerInfo {

    public final ServerProperties publicProperties;

    public final ServerProperties managementProperties;

    public DefaultServerInfo(ServerProperties publicProperties, ServerProperties managementProperties) {
        this.publicProperties = publicProperties;
        this.managementProperties = managementProperties;
    }

    @Override
    public ServerProperties getPublic() {
        return this.publicProperties;
    }

    @Override
    public ServerProperties getManagement() {
        return this.managementProperties;
    }
}
