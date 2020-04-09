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

package com.yanglinkui.grass.protocol.message.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.protocol.message.api.Message;

import java.util.HashMap;
import java.util.Map;

public class RabbitMQUtils {

    public static String getQueue(String topic, String routingKey) {
        if (Utils.isEmpty(topic)) {
            throw new IllegalArgumentException("topic cannot be null");
        }

        if (Utils.isNotEmpty(routingKey)) {
            topic = topic + "." + routingKey;
        }

        return topic;
    }

    public static Message mapToMessage(AMQP.BasicProperties properties, byte[] body) {
        Map<String, Object> headers = properties.getHeaders();
        Map<String, String> messageProperties = new HashMap<>();
        if (headers != null) {
            headers.entrySet().stream()
                    .filter(e -> (e.getValue() instanceof String))
                    .forEach(e -> messageProperties.put(e.getKey(), (String) e.getValue()));
        }

        return new Message(body)
                .setId(properties.getMessageId())
                .setContentType(properties.getContentType())
                .setContentEncoding(properties.getContentEncoding())
                .setCorrelationId(properties.getCorrelationId())
                .setProperties(messageProperties);
    }
}
