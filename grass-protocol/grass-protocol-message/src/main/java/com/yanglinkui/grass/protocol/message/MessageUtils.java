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

import com.yanglinkui.grass.ApplicationInstance;
import com.yanglinkui.grass.common.Utils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MessageUtils {

    public static String getTopicName(String prefix, String name) {
        if (Utils.isNotEmpty(prefix)) {
            name = prefix + "." + name;
        }

        return name;
    }

    public static String getGroup(ApplicationInstance instance) {
        return instance.getMetadata(MessageConstants.APPLICATION_METADATA_GROUP);
    }

    public static String getGroup(String prefix, String name) {
        if (Utils.isNotEmpty(prefix)) {
            name = prefix + "." + name;
        }

        return name;
    }

    public static final String getContainerType(ApplicationInstance instance) {
        return instance.getMetadata(MessageConstants.APPLICATION_METADATA_TYPE);
    }



    public static byte[] getCorrelationId() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = new byte[16]; // NOSONAR magic #
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bytes;
    }
}
