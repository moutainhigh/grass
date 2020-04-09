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

import static org.junit.jupiter.api.Assertions.*;

import com.rabbitmq.client.AMQP;
import com.yanglinkui.grass.protocol.message.api.Message;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class RabbitMQUtilsTest {

    @Test
    public void testGetQueue() {
        assertEquals("topic.routingKey", RabbitMQUtils.getQueue("topic", "routingKey"));
        assertEquals("topic", RabbitMQUtils.getQueue("topic", ""));
        assertEquals("topic", RabbitMQUtils.getQueue("topic", null));
        assertThrows(IllegalArgumentException.class, () -> RabbitMQUtils.getQueue("", null));
        assertThrows(IllegalArgumentException.class, () -> RabbitMQUtils.getQueue(null, null));
    }

    @Test
    public void testMapToMessage() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("key1", "value1");
        headers.put("key2", 503);
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.headers(headers)
                .replyTo("replyTo")
                .contentType("application/json")
                .contentEncoding("utf-8")
                .messageId("123")
                .correlationId("321")
                .appId("app1");

        Message message = RabbitMQUtils.mapToMessage(builder.build(), "jonas".getBytes());
        assertEquals("123", message.getId());
        assertEquals("321", message.getCorrelationId());
        assertEquals("application/json", message.getContentType());
        assertEquals("utf-8", message.getContentEncoding());
        assertEquals("jonas", new String(message.getBody()));

        Map<String, String> properties = message.getProperties();
        assertEquals(1, properties.size());
        assertEquals("value1", properties.get("key1"));
    }
}
