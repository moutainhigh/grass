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
import com.yanglinkui.grass.protocol.message.api.Message;
import com.yanglinkui.grass.protocol.message.api.Producer;
import com.yanglinkui.grass.protocol.message.api.RequestListener;
import com.yanglinkui.grass.protocol.message.api.SendCallback;
import com.yanglinkui.grass.protocol.message.support.Timer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class RabbitMQProducer implements Producer {
    static Logger logger = LoggerFactory.getLogger(RabbitMQProducer.class);

    private static final int READY = 0;
    private static final int RUNNING = 1;
    private static final int SHUTDOWN = 3;

    private final AtomicInteger status = new AtomicInteger(READY);

    private final CachingConnectionFactory factory;

    private final String group;
    private final String addresses;
    private final Map<String, String> config;

    private volatile Channel replyChannel;
    private volatile String replyQueue;

    private final ConcurrentSkipListMap<Long/* deliveryTag */, String/* messageId */> confirmTagList = new ConcurrentSkipListMap<>();
    private final Map<String/* messageId */, SendCallback> sendCallbackList = new ConcurrentHashMap<>(200, 0.75F);
    private final Map<String/* correlationId */, RequestListener> requestListenerList = new ConcurrentHashMap<>(200, 0.75F);
    private final Map<String/* messageId */, String/* correlationId*/> correlationIdList = new ConcurrentHashMap<>(200, 0.75F);

    public RabbitMQProducer(String group, String addresses, Map<String, String> config) {
        this.group = group;
        this.addresses = addresses;
        this.config = config;
        SendCallbackListener listener = new SendCallbackListener(this.sendCallbackList, this.correlationIdList,
                this.requestListenerList, this.confirmTagList);
        this.factory = new CachingConnectionFactory(addresses, config, listener, listener, null);
    }

    @Override
    public void send(String topic, String routingKey, Message message, SendCallback callback) throws IOException {
        if (this.status.get() != RUNNING) {
            throw new IllegalStateException("Producer is not running");
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

    @Override
    public void send(String topic, String routingKey, Message message, RequestListener listener, long timeout) throws IOException {
        if (this.status.get() != RUNNING) {
            throw new IllegalStateException("Producer is not running");
        }

        Channel channel = this.factory.getChannel();
        long tag = channel.getNextPublishSeqNo();

        try {
            addRequestListener(tag, message.getId(), message.getCorrelationId(), listener, timeout);
            AMQP.BasicProperties.Builder builder = createPropertiesBuilder(message, this.replyQueue);
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

    private void addRequestListener(long tag, String messageId, String correlationId, RequestListener listener, long timeout) {
        if (Utils.isEmpty(messageId)) {
            throw new IllegalArgumentException("messageId cannot be null");
        }

        this.confirmTagList.put(tag, messageId);
        if (Utils.isNotEmpty(correlationId) && listener != null) {
            this.correlationIdList.put(messageId, correlationId);
            this.requestListenerList.put(correlationId, listener);
            //设置timeout
            Timer.newTimeout(new RequestTimeoutTask(messageId, correlationId, timeout, this.requestListenerList),
                    timeout, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void start() throws Exception {
        if (this.status.compareAndSet(READY, RUNNING)) {
            this.replyChannel = this.factory.getChannel(ChannelType.SUBSCRIBE);
            this.replyQueue = this.replyChannel.queueDeclare("", false, true, false, null).getQueue();
            this.replyChannel.queueBind(this.replyQueue, group, this.replyQueue);
            this.replyChannel.basicConsume(this.replyQueue, false, new RequestConsumer(this.replyChannel, requestListenerList));
        } else {
            throw new IllegalStateException("The producer is not ready: " + this.status.get());
        }

    }

    @Override
    public void shutdown() throws IOException, TimeoutException {
        if (this.status.compareAndSet(RUNNING, SHUTDOWN)) {
            this.replyChannel.close();
            this.factory.destroy();
        } else {
            throw new IllegalStateException("The producer is not running: " + this.status.get());
        }
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
        return this.config;
    }
}
