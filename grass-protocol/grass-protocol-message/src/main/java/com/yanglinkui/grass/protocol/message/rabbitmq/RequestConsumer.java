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
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.protocol.message.api.Message;
import com.yanglinkui.grass.protocol.message.api.RequestListener;

import java.io.IOException;
import java.util.Map;

public class RequestConsumer extends DefaultConsumer {

    static Logger logger = LoggerFactory.getLogger(RequestConsumer.class);

    private final Map<String/* correlationId */, RequestListener> requestListenerMap;

    public RequestConsumer(Channel channel, Map<String, RequestListener> requestListenerMap) {
        super(channel);
        this.requestListenerMap = requestListenerMap;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        getChannel().basicAck(envelope.getDeliveryTag(), false);

        String correlationId = properties.getCorrelationId();
        RequestListener listener = this.requestListenerMap.get(correlationId);
        if (listener == null) {
            logger.error("Not found request listener: {}", correlationId);
        }

        Message message = RabbitMQUtils.mapToMessage(properties, body);
        listener.onSuccess(message);
    }
}
