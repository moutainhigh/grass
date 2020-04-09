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
import com.yanglinkui.grass.common.Utils;
import com.yanglinkui.grass.common.logger.Logger;
import com.yanglinkui.grass.common.logger.LoggerFactory;
import com.yanglinkui.grass.protocol.message.api.Consumer;
import com.yanglinkui.grass.protocol.message.api.Message;
import com.yanglinkui.grass.protocol.message.api.MessageListener;
import com.yanglinkui.grass.protocol.message.api.SendCallback;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class RabbitMQConsumer implements Consumer {

    static Logger logger = LoggerFactory.getLogger(RabbitMQConsumer.class);

    private static final String[] EMPTY_ROUTING_KEYS = new String[] {""};

    private static final int READY = 0;
    private static final int RUNNING = 1;
    private static final int SHUTDOWN = 3;

    private final AtomicInteger status = new AtomicInteger(READY);

    private String topic;
    private String[] routingKeys;
    private MessageListener listener;

    private final String group;
    private final String addresses;
    private final Map<String, String> config;
    private final ExecutorService executorService;
    private final CachingConnectionFactory factory;

    private final ConcurrentSkipListMap<Long/* deliveryTag */, String/* messageId */> confirmTagList = new ConcurrentSkipListMap<>();
    private final Map<String/* messageId */, SendCallback> sendCallbackList = new ConcurrentHashMap<>(200, 0.75F);

    private final List<Channel> channelList = new LinkedList<>();

    public RabbitMQConsumer(String group, String addresses, Map<String, String> config, ExecutorService executorService) {
        this.group = group;
        this.addresses = addresses;
        this.config = config;
        this.executorService = executorService;
        SendCallbackListener listener = new SendCallbackListener(this.sendCallbackList, null,
                null, this.confirmTagList);

        this.factory = new CachingConnectionFactory(addresses, config, listener, listener, executorService);
    }

    public void send(String topic, String routingKey, Message message, SendCallback callback) throws IOException {
        if (this.status.get() != RUNNING) {
            throw new IllegalStateException("Consumer is not running");
        }

        Channel channel = this.factory.getChannel();
        long tag = channel.getNextPublishSeqNo();

        try {
            addSendCallback(tag, message.getId(), callback);
            AMQP.BasicProperties.Builder builder = createPropertiesBuilder(message, null);
            channel.basicPublish(this.group, RabbitMQUtils.getQueue(topic, routingKey),
                    true, builder.build(), message.getBody());
        } finally {
            try {
                channel.close();
            } catch (Exception e) {
                logger.error("Failed to close channel", e);
            }
        }
    }

    private AMQP.BasicProperties.Builder createPropertiesBuilder(Message message, String replyQueue) {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.contentType(message.getContentType())
                .contentEncoding(message.getContentType())
                .correlationId(message.getCorrelationId())
                .messageId(message.getId())
                .replyTo(replyQueue)
                .headers((Map) message.getProperties());
        return builder;
    }


    private void addSendCallback(long tag, String messageId, SendCallback callback) {
        if (Utils.isEmpty(messageId)) {
            throw new IllegalArgumentException("messageId cannot be null");
        }

        this.confirmTagList.put(tag, messageId);

        if (callback != null) {
            this.sendCallbackList.put(messageId, callback);
        }
    }

    @Override
    public Consumer subscribe(String topic, String... routingKeys) {
        if (Utils.isEmpty(topic)) {
            throw new IllegalArgumentException("topic cannot be null");
        }
        this.topic = topic;
        this.routingKeys = routingKeys;
        return this;
    }

    @Override
    public Consumer setMessageListener(MessageListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public String getGroup() {
        return this.group;
    }

    @Override
    public String getAddresses() {
        return this.addresses;
    }

    @Override
    public Map<String, String> getConfig() {
        return Collections.unmodifiableMap(this.config);
    }

    CachingConnectionFactory getCachingConnectionFactory() {
        return this.factory;
    }

    @Override
    public void start() throws Exception {
        if (this.status.compareAndSet(READY, RUNNING)) {
            if (routingKeys == null) {
                routingKeys = EMPTY_ROUTING_KEYS;
            }

            if (routingKeys.length == 0) {
                routingKeys =  EMPTY_ROUTING_KEYS; //默认值
            }

            for (String routingKey : routingKeys) {
                String queue = RabbitMQUtils.getQueue(topic, routingKey);
                Channel channel = factory.getChannel(ChannelType.SUBSCRIBE);
                channel.queueDeclare(queue, false, false, false, null);
                channel.queueBind(queue, group, queue);
                channel.basicQos(1);
                channel.basicConsume(queue, false, new MessageConsumer(channel, this.listener, this));

                this.channelList.add(channel);
            }
        }

    }

    @Override
    public void shutdown() throws Exception {
        this.factory.destroy();
    }
}
