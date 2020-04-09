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

import com.yanglinkui.grass.protocol.message.api.MessagingFactory;
import com.yanglinkui.grass.protocol.message.rabbitmq.RabbitMQFactory;
import com.yanglinkui.grass.protocol.message.rocketmq.RocketMQFactory;

public class MessagingFactoryManager {

    public static MessagingFactory create(String type) {
        type = type != null ? type.toLowerCase() : null;
        if ("rabbitmq".equals(type)) {
            return new RabbitMQFactory();
        } else if ("rocketmq".equals(type)) {
            return new RocketMQFactory();
        } else {
            throw new IllegalArgumentException("Invalid message type: " + type);
        }
    }
}
