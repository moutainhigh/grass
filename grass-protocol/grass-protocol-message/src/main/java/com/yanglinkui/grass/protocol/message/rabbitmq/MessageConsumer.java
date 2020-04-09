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
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.protocol.message.api.*;

import java.io.IOException;
import java.util.function.BiConsumer;

public class MessageConsumer extends DefaultConsumer {

    static Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private final MessageListener listener;

    private final RabbitMQConsumer consumer;
    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public MessageConsumer(Channel channel, MessageListener listener, RabbitMQConsumer consumer) {
        super(channel);
        this.consumer = consumer;
        this.listener = listener;
    }

    public MessageListener getListener() {
        return listener;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        Message message = RabbitMQUtils.mapToMessage(properties, body);

        try {
            RabbitMQMessageContext context = createMessageContext(properties.getReplyTo(), envelope);
            listener.onMessage(context, message);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    RabbitMQMessageContext createMessageContext(String replyTo, Envelope envelope) {
        return new RabbitMQMessageContext(
                (message, callback) -> {
                    try {
                        if (Utils.isEmpty(replyTo)) {
                            callback.onException(new IllegalStateException("Not reply topic: " + message.getCorrelationId()));
                            return;
                        }
                        consumer.send(replyTo, null, message, callback);
                    } catch (IOException e) {
                        callback.onException(e);
                    }
                },
                () -> {
                    try {
                        getChannel().basicAck(envelope.getDeliveryTag(), false);
                    } catch (IOException e) {
                        logger.error("Failed to commit ack", e);
                    }
                });
    }

}
